package com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity;

public enum WorkStatus {
    OPEN("Aberta"),
    FREE("Livre"),
    CLOSED("Encerrada");

    private  final String workStatus;

    WorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public String getWorkStatus() {
        return workStatus;
    }
}
