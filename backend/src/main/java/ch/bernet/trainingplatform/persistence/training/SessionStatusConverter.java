package ch.bernet.trainingplatform.persistence.training;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SessionStatusConverter implements AttributeConverter<SessionStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SessionStatus status) {
        // status.getId() zurückgeben (null-sicher)
        if (status == null) {
            return null;
        }
        return status.getId();
    }

    @Override
    public SessionStatus convertToEntityAttribute(Integer id) {
        // SessionStatus.fromId(id) zurückgeben (null-sicher)
        if (id == null) {
            return null;
        }

        return SessionStatus.fromId(id);
    }

}
