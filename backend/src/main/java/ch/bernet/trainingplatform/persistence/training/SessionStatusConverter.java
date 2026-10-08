package ch.bernet.trainingplatform.persistence.training;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SessionStatusConverter implements AttributeConverter<SessionStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SessionStatus status) {
        // TODO: status.getId() zurückgeben (null-sicher)
        return null;
    }

    @Override
    public SessionStatus convertToEntityAttribute(Integer id) {
        // TODO: SessionStatus.fromId(id) zurückgeben (null-sicher)
        return null;
    }

}
