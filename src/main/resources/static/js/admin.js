document.addEventListener('DOMContentLoaded', () => {
    const usersTbody = document.getElementById('usersTbody');

    // CSRF из meta
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    const headersJson = () => {
        const h = { 'Content-Type': 'application/json' };
        if (csrfToken && csrfHeader) h[csrfHeader] = csrfToken;
        return h;
    };

    const roleLabel = (roleName) => roleName.replace('ROLE_', '');

    // ===== Error helpers =====
    async function readApiError(res) {
        let data = null;
        try {
            data = await res.json();
        } catch (e) {
            // например 204 или backend вернул не-json
        }

        const message = data?.message ?? `HTTP ${res.status}`;
        const fieldErrors = data?.fieldErrors ?? null;

        return { status: res.status, message, fieldErrors };
    }

    function ensureAlertContainer(formEl) {
        // создаём один контейнер под алерты в начале формы
        let box = formEl.querySelector('.js-form-alert');
        if (!box) {
            box = document.createElement('div');
            box.className = 'js-form-alert mb-3';
            formEl.prepend(box);
        }
        return box;
    }

    function clearFormError(formEl) {
        const box = formEl.querySelector('.js-form-alert');
        if (box) box.innerHTML = '';
    }

    function showFormError(formEl, html) {
        const box = ensureAlertContainer(formEl);
        box.innerHTML = `
          <div class="alert alert-danger py-2 mb-0" role="alert">
            ${html}
          </div>
        `;
    }

    function fieldErrorsToHtml(fieldErrors) {
        const items = Object.entries(fieldErrors).map(([field, msg]) => {
            return `<li><strong>${field}:</strong> ${msg}</li>`;
        }).join('');
        return `<div class="fw-semibold mb-1">Fix the following:</div><ul class="mb-0">${items}</ul>`;
    }

    // ===== Data loaders =====
    async function loadUsers() {
        const res = await fetch('/api/admin/users', { credentials: 'same-origin' });
        const users = await res.json();
        renderUsers(users);
    }

    function renderUsers(users) {
        usersTbody.innerHTML = '';
        users.forEach(u => {
            const rolesText = (u.roles || []).map(roleLabel).join(' ');
            const tr = document.createElement('tr');
            tr.innerHTML = `
        <td>${u.id}</td>
        <td>${u.name ?? ''}</td>
        <td>${u.surname ?? ''}</td>
        <td>${u.year ?? ''}</td>
        <td>${u.username ?? ''}</td>
        <td>${rolesText}</td>
        <td>
          <button class="btn btn-info text-white btn-edit" data-id="${u.id}" data-bs-toggle="modal" data-bs-target="#editModal">
            Edit
          </button>
        </td>
        <td>
          <button class="btn btn-danger btn-delete" data-id="${u.id}" data-bs-toggle="modal" data-bs-target="#deleteModal">
            Delete
          </button>
        </td>
      `;
            usersTbody.appendChild(tr);
        });
    }

    async function loadRolesInto(selectEl) {
        const res = await fetch('/api/roles', { credentials: 'same-origin' });
        const roles = await res.json(); // ["ROLE_ADMIN","ROLE_USER"]
        selectEl.innerHTML = '';
        roles.forEach(r => {
            const opt = document.createElement('option');
            opt.value = r;
            opt.textContent = roleLabel(r);
            selectEl.appendChild(opt);
        });
    }

    // ===== New User =====
    const newForm = document.getElementById('newUserForm');
    const newRoles = document.getElementById('newRoles');

    loadRolesInto(newRoles);

    // очищаем ошибки при открытии/вводе
    newForm.addEventListener('input', () => clearFormError(newForm));

    newForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearFormError(newForm);

        const payload = {
            name: document.getElementById('newName').value,
            surname: document.getElementById('newSurname').value,
            year: Number(document.getElementById('newYear').value) || null,
            username: document.getElementById('newUsername').value,
            password: document.getElementById('newPassword').value,
            roles: Array.from(newRoles.selectedOptions).map(o => o.value)
        };

        const res = await fetch('/api/admin/users', {
            method: 'POST',
            headers: headersJson(),
            body: JSON.stringify(payload),
            credentials: 'same-origin'
        });

        if (!res.ok) {
            const err = await readApiError(res);

            if (err.status === 400 && err.fieldErrors) {
                showFormError(newForm, fieldErrorsToHtml(err.fieldErrors));
            } else if (err.status === 409) {
                showFormError(newForm, err.message || 'User with same username already exists');
            } else {
                showFormError(newForm, err.message || 'Create failed');
            }
            return;
        }

        newForm.reset();
        await loadUsers();

        // переключение на вкладку Users table (если нужно)
        const usersTabBtn = document.querySelector('button[data-bs-target="#usersTable"]');
        usersTabBtn?.click();
    });

    // ===== Edit modal =====
    const editForm = document.getElementById('editUserForm');
    const editRoles = document.getElementById('editRoles');
    const editId = document.getElementById('editId');

    loadRolesInto(editRoles);

    editForm.addEventListener('input', () => clearFormError(editForm));

    usersTbody.addEventListener('click', async (e) => {
        const editBtn = e.target.closest('.btn-edit');
        if (!editBtn) return;

        clearFormError(editForm);

        const id = editBtn.dataset.id;
        const res = await fetch(`/api/admin/users/${id}`, { credentials: 'same-origin' });
        const u = await res.json();

        editId.value = u.id;
        document.getElementById('editName').value = u.name ?? '';
        document.getElementById('editSurname').value = u.surname ?? '';
        document.getElementById('editYear').value = u.year ?? '';
        document.getElementById('editUsername').value = u.username ?? '';
        document.getElementById('editPassword').value = '';

        const rolesSet = new Set(u.roles || []);
        Array.from(editRoles.options).forEach(opt => opt.selected = rolesSet.has(opt.value));
    });

    editForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearFormError(editForm);

        const id = editId.value;

        const payload = {
            name: document.getElementById('editName').value,
            surname: document.getElementById('editSurname').value,
            year: Number(document.getElementById('editYear').value) || null,
            username: document.getElementById('editUsername').value,
            password: document.getElementById('editPassword').value, // пусто = не менять
            roles: Array.from(editRoles.selectedOptions).map(o => o.value)
        };

        const res = await fetch(`/api/admin/users/${id}`, {
            method: 'PUT',
            headers: headersJson(),
            body: JSON.stringify(payload),
            credentials: 'same-origin'
        });

        if (!res.ok) {
            const err = await readApiError(res);

            if (err.status === 400 && err.fieldErrors) {
                showFormError(editForm, fieldErrorsToHtml(err.fieldErrors));
            } else if (err.status === 409) {
                showFormError(editForm, err.message || 'User with same username already exists');
            } else {
                showFormError(editForm, err.message || 'Update failed');
            }
            return;
        }

        // закрыть модалку
        const modalEl = document.getElementById('editModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        modal?.hide();

        await loadUsers();
    });

    // ===== Delete modal =====
    const deleteBtnConfirm = document.getElementById('deleteConfirmBtn');
    const deleteId = document.getElementById('deleteId');
    const deleteModalEl = document.getElementById('deleteModal');

    // покажем ошибки удаления в самом deleteModal (внутри)
    function showDeleteError(html) {
        const body = deleteModalEl.querySelector('.modal-body');
        let box = body.querySelector('.js-delete-alert');
        if (!box) {
            box = document.createElement('div');
            box.className = 'js-delete-alert mb-2';
            body.prepend(box);
        }
        box.innerHTML = `
          <div class="alert alert-danger py-2 mb-0" role="alert">${html}</div>
        `;
    }
    function clearDeleteError() {
        const body = deleteModalEl.querySelector('.modal-body');
        const box = body.querySelector('.js-delete-alert');
        if (box) box.innerHTML = '';
    }

    usersTbody.addEventListener('click', async (e) => {
        const delBtn = e.target.closest('.btn-delete');
        if (!delBtn) return;

        clearDeleteError();

        const id = delBtn.dataset.id;
        deleteId.value = id;

        const res = await fetch(`/api/admin/users/${id}`, { credentials: 'same-origin' });
        const u = await res.json();
        document.getElementById('deleteUserInfo').textContent =
            `${u.id} / ${u.username} / ${u.name} ${u.surname ?? ''}`;
    });

    deleteBtnConfirm.addEventListener('click', async () => {
        clearDeleteError();

        const id = deleteId.value;

        const res = await fetch(`/api/admin/users/${id}`, {
            method: 'DELETE',
            headers: headersJson(),
            credentials: 'same-origin'
        });

        if (!res.ok) {
            const err = await readApiError(res);
            showDeleteError(err.message || 'Delete failed');
            return;
        }

        const modal = bootstrap.Modal.getInstance(deleteModalEl);
        modal?.hide();

        await loadUsers();
    });

    // initial
    loadUsers();
});

