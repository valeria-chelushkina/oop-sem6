const toCamel = (str) => str.replace(/-([a-z])/g, (g) => g[1].toUpperCase());

export const getFormData = (formEl) => {
	if (!formEl) return {};
	const formData = new FormData(formEl);
    const payload = {};
	for (let [key, value] of formData.entries()) {
        const cleanKey = toCamel(key);
        payload[cleanKey] = typeof value === 'string' ? value.trim() : value;
    }
    return payload;
};

/**
 * Payload for CreateLoanRequest: Long ids, ISO date strings, enums as backend names.
 */
export const getLoanPayloadFromForm = (formEl) => {
    const raw = getFormData(formEl);
    const payload = { ...raw };

    const toLong = (key) => {
        const v = payload[key];
        if (v === '' || v == null) {
            delete payload[key];
            return;
        }
        const n = Number(v);
        if (!Number.isFinite(n)) {
            throw new Error(`Invalid number for ${key}`);
        }
        payload[key] = n;
    };

    toLong('bookItemId');
    toLong('readerId');
    if (payload.librarianId === '' || payload.librarianId == null) {
        delete payload.librarianId;
    } else {
        toLong('librarianId');
    }

    ['loanDate', 'dueDate', 'returnDate'].forEach((key) => {
        if (payload[key] === '' || payload[key] == null) {
            delete payload[key];
        }
    });

    return payload;
};

export const fillFormWithData = (formEl, data) => {
    if (!formEl || !data) return;

    Object.entries(data).forEach(([key, value]) => {
        const hyphenKey = key.replace(/[A-Z]/g, m => "-" + m.toLowerCase());
        const input = formEl.elements[key] || formEl.elements[hyphenKey];
        if (input) {
            input.value = value ?? "";
        }
    });
};

export const getBookPayload = (formEl) => {
    const formData = new FormData(formEl);
    const payload = {};

    payload.title = formData.get('title')?.trim();
    payload.isbn = formData.get('isbn')?.trim();
    payload.description = formData.get('description')?.trim();

    payload.authorIds = formData.getAll('author-ids');
    payload.genreIds = formData.getAll('genre-ids');

    const cover = formData.get('cover');
    if (cover && cover.size > 0) payload.cover = cover;

    return payload;
};

export const fillBookForm = (formEl, book) => {
    if (!formEl || !book) return;

    formEl.elements['title'].value = book.title ?? "";
    formEl.elements['isbn'].value = book.isbn ?? "";
    formEl.elements['description'].value = book.description ?? "";

    const setMultipleSelect = (name, values) => {
        const select = formEl.elements[name];
        if (!select) return;
        const idList = values?.map(v => String(v.id || v)) ?? [];
        Array.from(select.options).forEach(opt => {
            opt.selected = idList.includes(opt.value);
        });
    };

    setMultipleSelect('author-ids', book.authors);
    setMultipleSelect('genre-ids', book.genres);
};