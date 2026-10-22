'use client';

import { ContextProps } from '@/entities/entities';
import { useState, useEffect } from 'react';


export default function Contexto({
  titulo,
  usuarioId,
  modoAdmin,
}: ContextProps) {

  const [carregando, setCarregando] = useState(true);
  const [dados, setDados] = useState<string[]>([]);
  const [mensagem, setMensagem] = useState('');

  useEffect(() => {
    async function carregarDados() {
      setCarregando(true);

      try {
        // Exemplo: buscar dados usando usuarioId
        console.log('Carregando dados para:', usuarioId);

        setDados(['Dado 1', 'Dado 2']);
      } catch {
        setMensagem('Erro ao carregar os dados.');
      } finally {
        setCarregando(false);
      }
    }

    carregarDados();
  }, [usuarioId]);

  return (
    <section>
      <h2>{titulo}</h2>

      {carregando ? (
        <p>Carregando...</p>
      ) : (
        <ul>
          {dados.map((dado) => (
            <li key={dado}>{dado}</li>
          ))}
        </ul>
      )}

      {mensagem && <p>{mensagem}</p>}

      {modoAdmin && <button>Configurar</button>}
    </section>
  );
}