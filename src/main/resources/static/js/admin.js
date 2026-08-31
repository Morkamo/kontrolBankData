const adminModal = document.querySelector('[data-admin-modal]');
const adminOpenButton = document.querySelector('[data-admin-open]');
const adminCloseButton = document.querySelector('[data-admin-close]');
const lockChoice = document.querySelector('[data-lock-choice]');
const lockForm = document.querySelector('[data-database-lock-form]');
const lockConfirmButton = lockForm?.querySelector('button[type="submit"]');

adminOpenButton?.addEventListener('click', () => adminModal?.classList.add('modal-open'));
adminCloseButton?.addEventListener('click', () => adminModal?.classList.remove('modal-open'));
adminModal?.addEventListener('click', (event) => {
    if (event.target === adminModal) {
        adminModal.classList.remove('modal-open');
    }
});

lockChoice?.addEventListener('click', () => {
    lockChoice.classList.toggle('admin-option-selected');
    lockConfirmButton.disabled = !lockChoice.classList.contains('admin-option-selected');
});

lockForm?.addEventListener('submit', (event) => {
    if (!window.confirm(lockForm.dataset.confirmMessage)) {
        event.preventDefault();
    }
});

const userModal = document.querySelector('[data-user-modal]');
const userForm = document.querySelector('[data-user-form]');
const userModalTitle = document.querySelector('[data-user-modal-title]');
const userFields = [
    'id', 'username', 'fullName', 'password', 'userGroup', 'computerName', 'sendMessage',
    'userGroupForInteraction', 'activeOfRealTime', 'userGroupForOnvp', 'userGroupForStatic',
    'rank', 'stamp', 'departmentId'
];

document.querySelector('[data-user-create]')?.addEventListener('click', () => {
    userForm.reset();
    document.querySelector('[data-user-error]')?.setAttribute('hidden', '');
    userForm.action = userForm.dataset.createUrl;
    userModalTitle.textContent = 'Новый пользователь';
    userModal.classList.add('modal-open');
});

document.querySelectorAll('[data-user-edit]').forEach((button) => {
    button.addEventListener('click', () => {
        userForm.reset();
        document.querySelector('[data-user-error]')?.setAttribute('hidden', '');
        userForm.action = button.dataset.updateUrl;
        userModalTitle.textContent = 'Изменение пользователя';
        userFields.forEach((name) => {
            userForm.elements[name].value = button.dataset[name] ?? '';
        });
        userModal.classList.add('modal-open');
    });
});

document.querySelector('[data-user-close]')?.addEventListener('click', () => userModal.classList.remove('modal-open'));
userModal?.addEventListener('click', (event) => {
    if (event.target === userModal) {
        userModal.classList.remove('modal-open');
    }
});

document.querySelectorAll('.user-delete-form').forEach((form) => {
    form.addEventListener('submit', (event) => {
        if (!window.confirm(form.dataset.confirmMessage)) {
            event.preventDefault();
        }
    });
});
