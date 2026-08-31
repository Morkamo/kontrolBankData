const modal = document.querySelector('[data-journal-modal]');
const modalTitle = modal?.querySelector('.modal-title');
const openModalButtons = document.querySelectorAll('[data-open-modal]');
const cancelModalButton = modal?.querySelector('[data-close-modal]');
const journalForm = document.querySelector('.modal-form');
const deleteRecordForms = document.querySelectorAll('.delete-record-form');
const numericInputs = document.querySelectorAll('input[data-numeric]');
const nameInputs = document.querySelectorAll('input[data-person-name]');
const amountInputs = document.querySelectorAll('input[data-amount]');
const periodFieldsets = document.querySelectorAll('.period-fieldset, .period-search-fieldset');
const recordFormError = document.querySelector('[data-record-error]');
let modalClickStartedOutside = false;

function editableFieldNames() {
    return (journalForm?.dataset.editableFields || '')
        .replace(/[\[\]]/g, '')
        .split(',')
        .map((name) => name.trim())
        .filter(Boolean);
}

function setModalMode(mode) {
    if (!journalForm) {
        return;
    }

    const fullEdit = journalForm.dataset.canEditRecord === 'true';
    const editableFields = new Set(editableFieldNames());
    journalForm.querySelectorAll('.form-field').forEach((group) => {
        const createOnly = group.hasAttribute('data-create-only');
        const fields = group.querySelectorAll('[name]');
        const showGroup = createOnly ? mode === 'create' : mode !== 'partial'
            || Array.from(fields).some((field) => editableFields.has(field.dataset.permissionField || field.name));

        group.hidden = !showGroup;
        fields.forEach((field) => {
            field.disabled = !showGroup;
            field.dataset.editField = field.dataset.permissionField || field.name;
        });
    });

    journalForm.dataset.mode = mode;
    if (mode === 'edit' && fullEdit) {
        journalForm.dataset.mode = 'full-edit';
    }
}

if (modal?.classList.contains('modal-open')) {
    const editMode = journalForm?.dataset.initialEditMode === 'true';
    setModalMode(editMode && journalForm?.dataset.canEditRecord !== 'true' ? 'partial'
        : editMode ? 'edit' : 'create');
}

function closeModal() {
    modal?.classList.remove('modal-open');
}

function toDateInputValue(value) {
    const match = value?.match(/(\d{2})\.(\d{2})\.(\d{4})/);
    return match ? `${match[3]}-${match[2]}-${match[1]}` : value || '';
}

function setField(name, value) {
    const field = journalForm?.elements[name];

    if (field) {
        field.value = field.type === 'date' ? toDateInputValue(value) : value || '';
    }
}

function openCreateModal() {
    journalForm?.reset();
    recordFormError?.setAttribute('hidden', '');
    journalForm?.setAttribute('action', journalForm.dataset.createAction);
    setModalMode('create');

    if (modalTitle) {
        modalTitle.textContent = 'Новая запись';
    }

    modal?.classList.add('modal-open');
}

function openEditModal(button) {
    journalForm?.reset();
    recordFormError?.setAttribute('hidden', '');
    journalForm?.setAttribute('action', button.dataset.updateUrl);
    setModalMode(journalForm?.dataset.canEditRecord === 'true' ? 'edit' : 'partial');

    Object.entries(button.dataset).forEach(([name, value]) => setField(name, value));

    const periodDates = button.dataset.period?.match(/(\d{2}\.\d{2}\.\d{4})/g) || [];
    setField('periodStart', toDateInputValue(periodDates[0]));
    setField('periodEnd', toDateInputValue(periodDates[1]));

    if (modalTitle) {
        modalTitle.textContent = 'Изменение записи';
    }

    modal?.classList.add('modal-open');
}

openModalButtons.forEach((button) => {
    button.addEventListener('click', () => {
        if (button.dataset.updateUrl) {
            openEditModal(button);
        } else {
            openCreateModal();
        }
    });
});

cancelModalButton?.addEventListener('click', closeModal);

modal?.addEventListener('mousedown', (event) => {
    modalClickStartedOutside = event.target === modal;
});

modal?.addEventListener('click', (event) => {
    if (modalClickStartedOutside && event.target === modal) {
        closeModal();
    }

    modalClickStartedOutside = false;
});

deleteRecordForms.forEach((form) => {
    form.addEventListener('submit', (event) => {
        if (!window.confirm(form.dataset.confirmMessage || 'Удалить запись?')) {
            event.preventDefault();
        }
    });
});

numericInputs.forEach((input) => {
    input.addEventListener('input', () => {
        input.value = input.value.replace(/\D/g, '').slice(0, input.maxLength);
    });
});

nameInputs.forEach((input) => {
    input.addEventListener('input', () => {
        input.value = input.value.replace(/[^A-Za-zА-Яа-яЁё\s'-]/g, '');
    });
});

amountInputs.forEach((input) => {
    input.addEventListener('input', () => {
        input.value = input.value
            .replace(/[^0-9,.]/g, '')
            .replace(',', '.')
            .replace(/^(\d*\.?\d{0,2}).*$/, '$1');
    });
});

function validatePeriodDates(fieldset) {
    const start = fieldset.querySelector('input[name="periodStart"]');
    const end = fieldset.querySelector('input[name="periodEnd"]');

    if (!start || !end) {
        return true;
    }

    end.min = start.value || '';
    const invalid = start.value && end.value && start.value > end.value;
    end.setCustomValidity(invalid ? 'Дата ПО не может быть раньше даты С' : '');
    return !invalid;
}

periodFieldsets.forEach((fieldset) => {
    fieldset.querySelectorAll('input[type="date"]').forEach((input) => {
        input.addEventListener('input', () => validatePeriodDates(fieldset));
        input.addEventListener('change', () => validatePeriodDates(fieldset));
    });

    validatePeriodDates(fieldset);
});

journalForm?.addEventListener('submit', (event) => {
    const periodFieldset = journalForm.querySelector('.period-fieldset');

    if (periodFieldset && !validatePeriodDates(periodFieldset)) {
        event.preventDefault();
        periodFieldset.querySelector('input[name="periodEnd"]').reportValidity();
    }
});
