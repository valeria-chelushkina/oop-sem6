// will probably change it later?
export const LoanApi = {
	async getAll() {
        const response = await fetch(`/api/loans`);
        if (!response.ok) throw new Error(`Failed to fetch loans: ${response.status}`);
        const data = await response.json();
        return data;
    },
    async getById(id) {
        const response = await fetch(`/api/loans?id=${id}`);
        if (!response.ok) throw new Error(`Failed to fetch loans by id: ${response.status}`);
        const data = await response.json();
        return Array.isArray(data) ? data[0] : data;
    }
};