export function getAuthorPayloadFromForm(formEl) {
  const input = formEl?.querySelector("#author-pen-name");
  const penName = input?.value?.trim() ?? "";
  return { penName };
}

export function fillAuthorForm(formEl, author) {
  const input = formEl?.querySelector("#author-pen-name");
  if (input) {
    input.value = author?.penName ?? "";
  }
}