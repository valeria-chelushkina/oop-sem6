export function renderLoanUserRow(data) {
  return `
      <tr data-entry-id="${data.id}">
        <td scope="row">${data.id}</td>
        <td>${data.bookItemId ?? 'Unknown'} </td>
        <td>${data.loanDate ?? 'Unknown'}</td>
        <td>${data.dueDate ?? 'Unknown'}</td>
        <td>${data.returnDate || 'Not returned yet'}</td>
        <td>${data.loanType}</td>
        <td>${data.status}</td>
        <td>$
      </tr>
        `;
}