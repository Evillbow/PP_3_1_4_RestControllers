document.addEventListener('DOMContentLoaded', () => {
    const usersTbody = document.getElementById('usersTbody');

    // CSRF из meta (добавим в head fragment)
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    const headersJson = () => {
        const h = { 'Content-Type': 'application/json' };
        if (csrfToken && csrfHeader) h[csrfHeader] = csrfToken;
        return h;
    };

    const roleLabel = (roleName) => roleName.replace('ROLE_', '');

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

    newForm.addEventListener('submit', async (e) => {
        e.preventDefault();

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
            alert('Create failed (check console / server logs)');
            return;
        }

        newForm.reset();
        await loadUsers();

        // переключение на вкладку Users table (если хочешь)
        const usersTabBtn = document.querySelector('button[data-bs-target="#usersTable"]');
        usersTabBtn?.click();
    });

    // ===== Edit modal =====
    const editForm = document.getElementById('editUserForm');
    const editRoles = document.getElementById('editRoles');
    const editId = document.getElementById('editId');

    loadRolesInto(editRoles);

    usersTbody.addEventListener('click', async (e) => {
        const editBtn = e.target.closest('.btn-edit');
        if (!editBtn) return;

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
            alert('Update failed');
            return;
        }

        // закрыть модалку bootstrap’ом
        const modalEl = document.getElementById('editModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        modal?.hide();

        await loadUsers();
    });

    // ===== Delete modal =====
    const deleteBtnConfirm = document.getElementById('deleteConfirmBtn');
    const deleteId = document.getElementById('deleteId');

    usersTbody.addEventListener('click', async (e) => {
        const delBtn = e.target.closest('.btn-delete');
        if (!delBtn) return;

        const id = delBtn.dataset.id;
        deleteId.value = id;

        const res = await fetch(`/api/admin/users/${id}`, { credentials: 'same-origin' });
        const u = await res.json();
        document.getElementById('deleteUserInfo').textContent =
            `${u.id} / ${u.username} / ${u.name} ${u.surname ?? ''}`;
    });

    deleteBtnConfirm.addEventListener('click', async () => {
        const id = deleteId.value;

        const res = await fetch(`/api/admin/users/${id}`, {
            method: 'DELETE',
            headers: headersJson(),
            credentials: 'same-origin'
        });

        if (!res.ok) {
            alert('Delete failed');
            return;
        }

        const modalEl = document.getElementById('deleteModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        modal?.hide();

        await loadUsers();
    });

    // initial
    loadUsers();
});
