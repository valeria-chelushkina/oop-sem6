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
    payload.title = isEmpty('title', formData);
    payload.isbn = isEmpty('isbn', formData);
    payload.description = isEmpty('description', formData);
	payload.publisher = isEmpty('publisher', formData);
    payload.publisher = isEmpty('publisher', formData);
    const pubYear = formData.get('publication-year');
    payload.publicationYear = (pubYear && pubYear !== '') ? parseInt(pubYear) : null;
    payload.language = isEmpty('language', formData);
    const pages = formData.get('pages-count');
    payload.pagesCount = (pages && pages !== '') ? parseInt(pages) : null;
    payload.authorIds = formData.getAll('author-ids').map(id => Number(id)).filter(id => !isNaN(id));
    payload.genreIds = formData.getAll('genre-ids').map(id => Number(id)).filter(id => !isNaN(id));
    const coverURL = formData.get('coverURL');
    if (typeof coverURL === 'string' && coverURL.trim() !== '') {
        payload.coverURL = coverURL.trim();
    }
    return payload;
};

export const fillBookForm = (formEl, book) => {
    if (!formEl || !book) return;

    const fields = ['title', 'isbn', 'publisher', 'publication-year', 'language', 'pages-count', 'coverURL', 'description'];
    fields.forEach(fieldName => {
        if (formEl.elements[fieldName]) {
            formEl.elements[fieldName].value = book[toCamel(fieldName)] ?? "";
        }
    });

    const previewImg = formEl.querySelector('.cover-preview');
    if (previewImg) previewImg.src = book.coverURL || "";

	const setMultipleSelect = (name, values) => {
        const $select = $(formEl).find(`[name="${name}"]`)
        if (!$select.length) return;
        const idList = values?.map(v => String(v.id || v)) ?? [];
        $select.val(idList).trigger('change');
    };

    setMultipleSelect('author-ids', book.authors);
    setMultipleSelect('genre-ids', book.genres);
};

function isEmpty(name, formData){
	const elem = formData.get(name)?.trim();
	return elem === '' ? null : elem;
}