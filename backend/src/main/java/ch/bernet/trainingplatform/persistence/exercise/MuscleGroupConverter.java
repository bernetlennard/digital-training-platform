package ch.bernet.trainingplatform.persistence.exercise;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class MuscleGroupConverter implements AttributeConverter<MuscleGroup, Integer> {

    @Override
    public Integer convertToDatabaseColumn(MuscleGroup group) {
        // group.getId() zurückgeben (null-sicher)
        if (group == null) {
            return null;
        }
        return group.getId();
    }

    @Override
    public MuscleGroup convertToEntityAttribute(Integer id) {
        // MuscleGroup.fromId(id) zurückgeben (null-sicher)
        if (id == null) {
            return null;
        }
        return MuscleGroup.fromId(id);
    }

}
