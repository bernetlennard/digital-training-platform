package ch.bernet.trainingplatform.common;

public final class ApiStatus {

    public static final String OK = "Erfolgreich, die Antwort enthält einen Body";
    public static final String CREATED = "Ressource angelegt, der Body enthält die neue Ressource";
    public static final String NO_CONTENT = "Erfolgreich, die Antwort enthält keinen Body";
    public static final String BAD_REQUEST = "Das Request-DTO verletzt eine Validierungsregel";
    public static final String UNAUTHORIZED = "Kein oder ungültiges Token bzw. falsche Zugangsdaten";
    public static final String NOT_FOUND = "Die Ressource existiert nicht oder gehört einem anderen Sportler";
    public static final String CONFLICT = "Widerspruch zum gespeicherten Datenbestand, z. B. ein bereits vergebener Name";
    public static final String UNPROCESSABLE_CONTENT = "Fachliche Regel verletzt, die sich aus dem Request allein ergibt, z. B. ein Datum in der Vergangenheit";
    public static final String TOO_MANY_REQUESTS = "Login nach zu vielen Fehlversuchen vorübergehend gesperrt";

    private ApiStatus() {
    }

}
