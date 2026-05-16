package pt.florinhas.marcacoes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.florinhas.marcacoes.domain.SystemConfig;
import pt.florinhas.marcacoes.repository.SystemConfigRepository;

@Service
public class SystemConfigService {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    public String getConfigValue(String key, String defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
            .map(SystemConfig::getConfigValue)
            .orElse(defaultValue);
    }

    public int getConfigValueAsInt(String key, int defaultValue) {
        try {
            String value = getConfigValue(key, String.valueOf(defaultValue));
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Transactional
    public void setConfigValue(String key, String value, String description) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
            .orElse(SystemConfig.builder()
                .configKey(key)
                .build());
        
        config.setConfigValue(value);
        if (description != null) {
            config.setDescription(description);
        }
        
        systemConfigRepository.save(config);
    }
}