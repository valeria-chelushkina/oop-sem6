import { UrlService } from '../services/urlService.js';


async function updateHeader() {
		const response = await fetch('/api/auth/status');
		const auth = await response.json();
		const authContainer = document.getElementById('auth-container');
		if(auth.authenticated) {
			let roleTitle ='';
			let roleRef = '';
			if(auth.role === 'READER') {
				roleTitle = 'Orders';
				roleRef='#' //temporary
			}
			else{
				roleTitle = 'Management panel';
				roleRef="/management"
			}
			// # - are temporary
			authContainer.innerHTML = `
				<div class="profile-menu">
                    <p>${auth.firstName} ${auth.lastName}</p>
                    <div class="drop-down-menu">
                        <a href="#"><div>Profile</div></a>
                        <hr>
                        <a href=${roleRef}><div>${roleTitle}</div></a>
                        <hr>
                        <a href="/logout"><div>Log out</div></a>
                    </div>
                </div>
			`;
		}
		else{
			authContainer.innerHTML = `
				<a href="/login" class="sign-in-btn">Sign in</a>
            `;
		}
	};

function searchFunction() {
	const searchBar = document.getElementById('search-bar');
	const searchForm = document.getElementById('search-form');
    const filterForm = document.getElementById('filter-form');
	[searchForm, filterForm].forEach(form => {
        form?.addEventListener('submit', (e) => {
            e.preventDefault();
            const params = UrlService.getParamsFromForm(searchBar, filterForm);
            UrlService.navigateWithParams(params);
        });
    });
}

document.addEventListener('DOMContentLoaded', () => {
	updateHeader();
	searchFunction();
});