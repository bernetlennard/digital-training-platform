// Liest das JWT aus dem Set-Cookie-Header und speichert es als {{token}}.
// Das Cookie ist "Secure" und wird vom HTTP-Client über http://localhost nicht
// automatisch mitgeschickt, deshalb wird es explizit im Cookie-Header gesetzt.
const setCookie = response.headers.valueOf("Set-Cookie");
const match = setCookie && setCookie.match(/access_token=([^;]*)/);
if (match) {
    client.global.set("token", match[1]);
}
