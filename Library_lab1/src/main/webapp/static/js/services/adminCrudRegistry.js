import { modalConfigs } from './adminConfig.js';
import { GenreApi } from '../api/genreApi.js';
import { AuthorApi } from '../api/authorApi.js';

import { openEditModal, saveEntity, deleteEntity } from './adminCrudService.js';
import { getFormData, fillFormWithData } from '../utils/adminCrudUtils.js';

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
    },
  // add other ones
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
  if (!crud) throw new Error("CRUD not implemented for this section");
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
  if (!crud) throw new Error("CRUD not implemented for this section");
  return saveEntity({
    mode,
    id,
    formEl,
    getPayloadFromForm: crud.form.getPayloadFromForm,
    validate: crud.form.validate,
    apiCreate: crud.api.create,
    apiUpdate: crud.api.update,
  });
}

export async function deleteForSection({ sectionId, id }) {
  const crud = getCrudForSection(sectionId);
  if (!crud) throw new Error("CRUD not implemented for this section");
  return deleteEntity({ id, apiDelete: crud.api.delete });
  }