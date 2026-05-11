import { LoanApi } from '../api/loanApi.js';
import { sectionFilterConfigs } from './adminFiltersConfig.js';
import { renderCheckboxGroup, LOAN_TYPES, renderLoanDateRange } from '../components/adminFilterTemplates.js';

const LOAN_STATUSES = {
    ORDERED: 'ORDERED',
    ISSUED: 'ISSUED',
    RETURNED: 'RETURNED',
    LOST: 'LOST',
    DAMAGED: 'DAMAGED',
    OVERDUE: 'OVERDUE'
};

const LIBRARIAN_LOAN_STATUSES = ["ORDERED", "ISSUED", "OVERDUE"];

export function getOrdersConfig(isLibrarian) {
    if (isLibrarian) {
        return {
            title: "Ongoing Orders",
            headers: ["ID", "Book item ID", "Reader ID", "Loan date", "Due date", "Type", "Status", "Actions"],
            renderer: (item) => {
                let actions = '';
                if (item.status === LOAN_STATUSES.ORDERED) {
                    actions = `
                        <button class="action-btn issue-btn" data-id="${item.id}">Issue</button>
                        <button class="action-btn delete-btn" data-id="${item.id}">Cancel</button>
                    `;
                } else if (item.status === LOAN_STATUSES.ISSUED || item.status === LOAN_STATUSES.OVERDUE) {
                    actions = `<button class="action-btn return-btn" data-id="${item.id}">Return</button>`;
                }

                return `
                    <tr data-entry-id="${item.id}">
                        <td>${item.id}</td>
                        <td>${item.bookItemId}</td>
                        <td>${item.readerId}</td>
                        <td>${item.loanDate || '-'}</td>
                        <td>${item.dueDate || '-'}</td>
                        <td>${item.loanType}</td>
                        <td><span class="status-badge status-${item.status.toLowerCase()}">${item.status}</span></td>
                        <td>${actions}</td>
                    </tr>
                `;
            },
            apiCall: async () => {
                const allLoans = await LoanApi.getAll();
                return allLoans.filter(l => 
                    l.status === LOAN_STATUSES.ORDERED || 
                    l.status === LOAN_STATUSES.ISSUED || 
                    l.status === LOAN_STATUSES.OVERDUE
                );
            },
            ...sectionFilterConfigs["loans-section"],
            filterTemplate: () => `
                ${renderCheckboxGroup("loanType", LOAN_TYPES, "Loan type")}
                ${renderCheckboxGroup("loanStatus", LIBRARIAN_LOAN_STATUSES, "Status")}
                ${renderLoanDateRange("loanDate", "Loan date")}
                ${renderLoanDateRange("dueDate", "Due date")}
            `
        };
    } else {
        return {
            title: "My Orders",
            headers: ["ID", "Book item ID", "Loan date", "Due date", "Return date", "Type", "Status"],
            renderer: (item) => `
                <tr data-entry-id="${item.id}">
                    <td>${item.id}</td>
                    <td>${item.bookItemId}</td>
                    <td>${item.loanDate || '-'}</td>
                    <td>${item.dueDate || '-'}</td>
                    <td>${item.returnDate || '-'}</td>
                    <td>${item.loanType}</td>
                    <td><span class="status-badge status-${item.status.toLowerCase()}">${item.status}</span></td>
                </tr>
            `,
            apiCall: async (userId) => {
                const allLoans = await LoanApi.getAll();
                return allLoans.filter(loan => loan.readerId === userId);
            },
            ...sectionFilterConfigs["loans-section"]
        };
    }
}
