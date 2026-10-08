package ch.bernet.trainingplatform.persistence.exercise;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class MuscleGroupConverter implements AttributeConverter<MuscleGroup, Integer> {

    @Override
    public Integer convertToDatabaseColumn(MuscleGroup group) {
        // TODO: group.getId() zurückgeben (null-sicher)
        return null;
    }

    @Override
    public MuscleGroup convertToEntityAttribute(Integer id) {
        // TODO: MuscleGroup.fromId(id) zurückgeben (null-sicher)
        return null;
    }

}
