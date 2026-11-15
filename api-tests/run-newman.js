// Draait de collectie met Newman. Het wachtwoord komt uit de omgevingsvariabele QA_PASSWORD
// (nooit uit de repository). Werkt op Windows, macOS en Linux: `npm test`.
const newman = require('newman');

const password = process.env.QA_PASSWORD;
if (!password) {
  console.error('QA_PASSWORD is niet gezet. Voorbeeld: QA_PASSWORD=... npm test');
  process.exit(1);
}

const envVar = [{ key: 'password', value: password }];
if (process.env.BASE_URL) envVar.push({ key: 'base_url', value: process.env.BASE_URL });
if (process.env.KEYCLOAK_URL) envVar.push({ key: 'keycloak_url', value: process.env.KEYCLOAK_URL });

newman.run({
  collection: require('./postman/qacademy-team2.postman_collection.json'),
  environment: require('./postman/qacademy-team2.postman_environment.json'),
  envVar,
  reporters: ['cli', 'htmlextra', 'junit', 'json'],
  reporter: {
    htmlextra: { export: 'reports/report.html', title: 'QAcademy team 2 - API tests' },
    junit: { export: 'reports/junit.xml' },
    json: { export: 'reports/newman.json' }
  },
  timeoutRequest: 30000
}, (err, summary) => {
  if (err) { console.error(err); process.exit(1); }
  process.exit(summary.run.failures.length ? 1 : 0);
});
