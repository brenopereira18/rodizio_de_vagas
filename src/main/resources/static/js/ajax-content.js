document.addEventListener('DOMContentLoaded', () => {
  const links = document.querySelectorAll('.menu-link');
  const contentDiv = document.getElementById('content');

  links.forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      fetch(link.href, {
        headers: {
          'X-Requested-With': 'XMLHttpRequest'
        }
      })
      .then(response => response.text())
      .then(html => {
        contentDiv.innerHTML = html;
      })
      .catch(error => {
        contentDiv.innerHTML = '<p>Erro ao carregar conteúdo.</p>';
      });
    });
  });
});
