package pt.florinhas.common_data.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Converter
public class NifEncryptorConverter implements AttributeConverter<String, String> {

    private static CryptoUtils cryptoUtils;

    @Autowired
    public void setCryptoUtils(CryptoUtils utils) {
        NifEncryptorConverter.cryptoUtils = utils;
    }

    @Override
    public String convertToDatabaseColumn(String nif) {
        if (nif == null) return null;
        if (cryptoUtils == null) {
            throw new IllegalStateException("CryptoUtils não inicializado — verifique a configuração de app.security.encryption.key");
        }
        if (nif.contains(":")) return nif;
        return cryptoUtils.encrypt(nif);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        if (cryptoUtils == null) {
            throw new IllegalStateException("CryptoUtils não inicializado — verifique a configuração de app.security.encryption.key");
        }
        if (!dbData.contains(":")) return dbData;
        return cryptoUtils.decrypt(dbData);
    }
}
