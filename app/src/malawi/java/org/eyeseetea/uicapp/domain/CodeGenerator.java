package org.eyeseetea.uicapp.domain;

public class CodeGenerator extends CodeGeneratorBase {

    @Override
    protected String generateCodeValue(Client client) {

        String code = "";

        code += GeneratorUtils.extractLetters(client.getMother(),
                client.getMother().length() - 2);

        code += GeneratorUtils.extractLetters(client.getSurname(),
                client.getSurname().length() - 2);

        code += GeneratorUtils.extractLetters(client.getDistrict(),
                client.getDistrict().length() - 2);

        code += GeneratorUtils.formatDate(client.getDateOfBirth());
        code += GeneratorUtils.extractLetters(client.getSex(), 0, 1);
        code += client.getTwinNumber();


        return code;
    }
}