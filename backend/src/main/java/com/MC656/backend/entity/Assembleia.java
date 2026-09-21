package com.MC656.backend.entity;

import java.util.ArrayList;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;

// "Assembleia" deve herdar de "Contexto"
@Entity // A classe representa uma entidade no banco de dados
public class Assembleia extends Contexto {
  
    @ElementCollection 
    private ArrayList<String> pautas;
    
    private int participantes;

    // Construtor
    public Assembleia(String nome, int participantes){
        super(nome, TipoContexto.ASSEMBLEIA);
        this.pautas = new ArrayList<>();
        this.participantes = participantes;
        validar_assembleia();
    }

    // Construtor sem argumentos, para ser utilizado pelo banco de dados
    protected Assembleia(){}

    // Retorna a lista de pautas da assembleia
    public ArrayList<String> getPautas(){
        return pautas;
    }

    // Adiciona uma nova pauta à lista de pautas
    public void adicionaPauta(String pauta){
        pautas.add(pauta);
    }

    public int getParticipantes(){
        return participantes;
    }

    public void setParticipantes(int participantes){

        // A assembleia deve ter ao menos 1 participante (o criador)
        if (participantes <= 0){
            throw new IllegalArgumentException("A assembleia deve ter ao menos um participante.");
        }
        this.participantes = participantes;
    }

    // Confere se a entidade "Assembleia" poderá ser criada sem erros
    public void validar_assembleia(){

        // A assembleia deve ter ao menos 1 participante (o criador)
        if (participantes <= 0){
            throw new IllegalArgumentException("A assembleia deve ter ao menos um participante.");
        }
    }
}
