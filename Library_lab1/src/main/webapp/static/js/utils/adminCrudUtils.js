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

    payload.title = formData.get('title')?.trim();
        payload.isbn = formData.get('isbn')?.trim();
        payload.description = formData.get('description')?.trim();
        payload.publisher = formData.get('publisher')?.trim();

        const pubYear = formData.get('publication-year');
        payload.publicationYear = (pubYear && pubYear !== '') ? parseInt(pubYear) : null;

        payload.language = formData.get('language')?.trim();

        const pages = formData.get('pages-count');
        payload.pagesCount = (pages && pages !== '') ? parseInt(pages) : null;

        // Map authors and genres as IDs for the backend to process
        payload.authorIds = formData.getAll('author-ids').map(id => Number(id)).filter(id => !isNaN(id));
        payload.genreIds = formData.getAll('genre-ids').map(id => Number(id)).filter(id => !isNaN(id));

        // Cover handling (simplified to existing URL if present)
        const coverURL = formData.get('coverURL');
        if (typeof coverURL === 'string' && coverURL.trim() !== '') {
            payload.coverURL = coverURL.trim();
        }

        return payload;
};

export const fillBookForm = (formEl, book) => {
    if (!formEl || !book) return;

    if (formEl.elements['title']) formEl.elements['title'].value = book.title ?? "";
        if (formEl.elements['isbn']) formEl.elements['isbn'].value = book.isbn ?? "";
        if (formEl.elements['publisher']) formEl.elements['publisher'].value = book.publisher ?? "";
        if (formEl.elements['publication-year']) formEl.elements['publication-year'].value = book.publicationYear ?? "";
        if (formEl.elements['language']) formEl.elements['language'].value = book.language ?? "";
        if (formEl.elements['pages-count']) formEl.elements['pages-count'].value = book.pagesCount ?? "";
        if (formEl.elements['description']) formEl.elements['description'].value = book.description ?? "";

        const previewImg = formEl.querySelector('.cover-preview');
        if (previewImg) previewImg.src = book.coverURL || "";

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