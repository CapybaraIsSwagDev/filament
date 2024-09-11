package de.tomalbrc.filament.behaviour;

import com.google.gson.*;
import de.tomalbrc.filament.Filament;
import de.tomalbrc.filament.api.behaviour.Behaviour;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiConsumer;

public class BehaviourConfigMap {
    private final Map<BehaviourType<? extends Behaviour<?>,?>, Object> behaviourConfigMap = new Object2ObjectOpenHashMap<>();

    public void put(BehaviourType<?,?> type, Object config) {
        this.behaviourConfigMap.put(type, config);
    }

    public <T extends Behaviour<E>,E> E get(BehaviourType<T,E> type) {
        return (E) this.behaviourConfigMap.get(type);
    }

    public <T extends Behaviour<E>,E> boolean has(BehaviourType<T,E> type) {
        return this.behaviourConfigMap.containsKey(type);
    }

    public <T extends Behaviour<E>,E> void forEach(BiConsumer<BehaviourType<T,E>, Object> biConsumer) {
        for (Map.Entry<BehaviourType<? extends Behaviour<?>, ?>, Object> entry : this.behaviourConfigMap.entrySet()) {
            biConsumer.accept((BehaviourType<T, E>) entry.getKey(), entry.getValue());
        }
    }

    public boolean isEmpty() {
        return this.behaviourConfigMap.isEmpty();
    }

    public static class Deserializer implements JsonDeserializer<BehaviourConfigMap> {
        @Override
        public BehaviourConfigMap deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();
            BehaviourConfigMap behaviourConfigMap = new BehaviourConfigMap();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                ResourceLocation resourceLocation;
                if (entry.getKey().contains(":"))
                    resourceLocation = ResourceLocation.parse(entry.getKey());
                else
                    resourceLocation = ResourceLocation.fromNamespaceAndPath("filament", entry.getKey());

                var behaviourType = BehaviourRegistry.getType(resourceLocation);
                var clazz = behaviourType.configType();

                if (clazz == null) {
                    Filament.LOGGER.error("Could not load behaviour " + resourceLocation);
                    continue;
                }

                Object deserialized = jsonDeserializationContext.deserialize(entry.getValue(), clazz);
                behaviourConfigMap.put(BehaviourRegistry.getType(resourceLocation), deserialized);
            }
            return behaviourConfigMap;
        }
    }
}