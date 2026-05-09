import { modalConfigs } from './adminConfig.js';
import { GenreApi } from '../api/genreApi.js';
import { AuthorApi } from '../api/authorApi.js';
import { BookItemApi } from '../api/bookItemApi.js';
import { LoanApi } from '../api/loanApi.js';
import { BookApi } from '../api/bookApi.js';

import { openEditModal, saveEntity, deleteEntity } from './adminCrudService.js';
import { getFormData, fillFormWithData, getLoanPayloadFromForm, fillBookForm, getBookPayload } from '../utils/adminCrudUtils.js';
import { appendAndSelect } from '../utils/adminUtils.js';

export const adminCrudRegistry = {
	"genres-section": {
        modalKey: "add-genre",
        titles: {
        update: "Update genre",
        },
        api: {
        getById: GenreApi.getById,
        create: GenreApi.create,
        update: GenreApi.update,
        delete: GenreApi.delete,
        },
        form: {
            fillForm: fillFormWithData,
            getPayloadFromForm: getFormData,
            validate(payload) {
            if (!payload?.name) throw new Error("Genre name is required");
        },
        },
        appendSelect: (data) => {
            appendAndSelect('[name="genre-ids"]', data, 'name');
        }
    },
	"authors-section": {
        modalKey: "add-author",
        titles: {
        update: "Update author",
        },
        api: {
        getById: AuthorApi.getById,
        create: AuthorApi.create,
        update: AuthorApi.update,
        delete: AuthorApi.delete,
        },
        form: {
            fillForm: fillFormWithData,
            getPayloadFromForm: getFormData,
            validate(payload) {
            if (!payload?.penName) throw new Error("Author pen name is required");
        },
        },
        appendSelect: (data) => {
                    appendAndSelect('[name="author-ids"]', data, 'penName');
                }
    },
	"book-items-section": {
        modalKey: "add-book-item",
        titles: {
        update: "Update book item",
        },
        api: {
        getById: BookItemApi.getById,
        create: BookItemApi.create,
        update: BookItemApi.update,
        delete: BookItemApi.delete,
        },
        form: {
            fillForm: fillFormWithData,
            getPayloadFromForm: getFormData,
            validate(payload) {
            if (!payload?.bookId) throw new Error("Book id is required");
            if(!payload.status) throw new Error("Status is required");
        },
        },
        appendSelect: () => null,
    },
	"loans-section": {
        modalKey: "add-loan",
        titles: {
        update: "Update loan",
        },
        api: {
        getById: LoanApi.getById,
        create: LoanApi.create,
        update: LoanApi.update,
        delete: LoanApi.delete,
        },
        form: {
            fillForm: fillFormWithData,
            getPayloadFromForm: getLoanPayloadFromForm,
            validate(payload) {
            if (!payload?.bookItemId) throw new Error("Book item id is required");
            if (!payload?.readerId) throw new Error("Reader id is required");
            if(!payload?.status) throw new Error("Status is required");
            if(!payload?.loanType) throw new Error("Type is required");
        },
        },
        appendSelect: () => null,
    },
	"books-section": {
        modalKey: "add-book",
        titles: {
        update: "Update book",
        },
        api: {
        getById: BookApi.getById,
        create: BookApi.create,
        update: BookApi.update,
        delete: BookApi.delete,
        },
        form: {
            fillForm: fillBookForm,
            getPayloadFromForm: getBookPayload,
            validate(payload) {
            if(!payload?.title) throw new Error("Title is required");
            if(!payload?.pagesCount) throw new Error("Page number is required");
        },
        },
        appendSelect: () => null,
    }
}

export function getCrudForSection(sectionId) {
  return adminCrudRegistry[sectionId] || null;
}

export function getModalRendererFor(crud) {
  const modal = modalConfigs[crud.modalKey];
  if (!modal?.renderer) throw new Error(`Modal renderer not found for key: ${crud.modalKey}`);
  return modal.renderer;
}

export async function openEditForSection({ sectionId, id, overlayEl, titleEl, formEl, setModalContext }) {
  const crud = getCrudForSection(sectionId);
  if (!crud) throw new Error("No section found.");
  return openEditModal({
    id,
    apiGetById: crud.api.getById,
    overlayEl,
    titleEl,
    titleText: crud.titles?.update || "Update",
    formEl,
    renderFormHtml: getModalRendererFor(crud),
    fillForm: crud.form.fillForm,
    setModalContext,
    modalContext: { section: sectionId },
  });
}

export async function saveForSection({ sectionId, mode, id, formEl }) {
  const crud = getCrudForSection(sectionId);
  if (!crud) throw new Error("No section found.");
  const payload = crud.form.getPayloadFromForm(formEl);
  const responseData = await saveEntity({
    mode,
    id,
    formEl,
    getPayloadFromForm: crud.form.getPayloadFromForm,
    validate: crud.form.validate,
    apiCreate: crud.api.create,
    apiUpdate: crud.api.update,
  });
  const finalData = { ...payload, ...responseData };
  if (crud.appendSelect && typeof crud.appendSelect === "function") {
      crud.appendSelect(finalData);
    }
  return finalData;
}

export async function deleteForSection({ sectionId, id }) {
  const crud = getCrudForSection(sectionId);
  if (!crud) throw new Error("No section found.");
  return deleteEntity({ id, apiDelete: crud.api.delete });
  }
