package com.dioburger.channels;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Junta todos los {@link ChannelAdapter} disponibles en un registro indexado
 * por {@link ChannelName}, para que el controller no tenga que saber qué
 * implementación concreta usar por canal.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
@Service
public class ChannelRegistryService {

    private final Map<ChannelName, ChannelAdapter> adapters;

    public ChannelRegistryService(List<ChannelAdapter> channelAdapters) {
        this.adapters = channelAdapters.stream()
                .collect(Collectors.toUnmodifiableMap(ChannelAdapter::getChannelName, Function.identity()));
    }

    public ChannelAdapter getAdapter(ChannelName channel) {
        ChannelAdapter adapter = adapters.get(channel);
        if (adapter == null) {
            throw new IllegalArgumentException("No hay adapter registrado para el canal: " + channel);
        }
        return adapter;
    }
}
