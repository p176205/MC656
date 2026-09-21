// OBS: A existência do "representante" como parte dos atributos do Condominio foi apagada temporariamente, 
// pois os testes relacionados a essa funcionalidade exigem a implementação completa da classe Usuario e do relacionamento Participa (entre usuários e contextos),
// que só serão finalizados na próxima entrega

package com.MC656.backend.entity;

import jakarta.persistence.Entity;
//import jakarta.persistence.ManyToOne;

// "Condominio" deve herdar de "Contexto"
@Entity // A classe representa uma entidade no banco de dados
public class Condominio extends Contexto {

    // TO DO: Finalizar a implementação do representante do comdomínio (entrega 3)
    //@ManyToOne 
    //private Usuario representante;

    private int total_moradores;
    
    // Construtor
    public Condominio(String nome, int total_moradores){
        super(nome, TipoContexto.CONDOMINIO);
        //this.representante = representante;
        this.total_moradores = total_moradores;
        validar_condominio();
    }

    // Construtor sem argumentos, para ser utilizado pelo banco de dados
    protected Condominio(){}

    // Obtém o representante (síndico) atual do condomínio
    //public Usuario getRepresentante(){
    //    return representante;
    //}

    // Obtém o número de moradores atual do condomínio
    public int getTotalMoradores(){
        return total_moradores;
    }

    // Adiciona ou modifica o representante (síndico) atual do condomínio
    //public void setRepresentante(Usuario representante){
    //    this.representante = representante;
    //    validar_condominio();
    //}

    // Adiciona ou modifica o número de moradores atual do condomínio
    public void setTotalMoradores(int total_moradores){
        this.total_moradores = total_moradores;
        validar_condominio();
    }

    // Confere se a entidade "Condominio" poderá ser criada sem erros
    public final void validar_condominio(){

        // O "Condominio" deve ter um representante (síndico) sempre
        //if (representante == null){
        //    throw new IllegalArgumentException("O representante do condomínio deve ser informado.");
        //}
        // O "Condominio" deve ter ao menos 1 morador (o representante)
        if (total_moradores <= 0){
            throw new IllegalArgumentException("O condomínio deve ter ao menos um morador.");
        }
    }
}
