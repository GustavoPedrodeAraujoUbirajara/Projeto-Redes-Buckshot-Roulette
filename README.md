# Projeto-Redes-Buckshot-Roulette
Trabalho apresentado ao professor David Alain do Nascimento como pré-requisito avaliativo do IV bimestre da disciplina de Redes de Computadores do curso Técnico Integrado em Informática, 3º Ano, período da tarde.

## Participantes
__Bruno Honorato da Silva;__
__Daniel Oliveira Rosendo;__
__Gustavo Pedro de Araujo Ubirajara;__
__Julio Mendes de Lira Alves;__

## Descrição do Jogo
Inspirado no jogo de computador Buckshot Roulette, criamos este jogo como uma homenagem tanto ao título original quanto aos antigos jogos de computador que funcionavam exclusivamente com base em texto. Neste jogo, você e mais três amigos participam de uma disputa perigosa, na qual apenas um jogador poderá sair com o prêmio final de 1 milhão de reais. Todos os participantes começam com 3 vidas, e uma quantidade desconhecida de munições reais e falsas é carregada em uma arma. A cada rodada, os jogadores devem escolher entre atirar em si mesmos ou atirar em um dos outros participantes, assumindo os riscos de cada decisão. O vencedor será aquele que permanecer vivo ao final do jogo.

## Como Executar o Projeto
Primeiro, execute o arquivo Servidor.java como uma Java Application. Em seguida, execute o arquivo Cliente.java também como uma Java Application.

## Como Jogar
O modo de jogo é relativamente simples e baseado em turnos. Ao iniciar a partida, deve-se cadastrar o nome dos três jogadores. Em seguida, uma imagem de um gatinho será exibida no console. Após isso, o usuário deve alternar para a aba cliente, digitar o comando start e, em seguida, begin, iniciando oficialmente o jogo. Assim que a partida começa, o sistema informa a quantidade total de balas reais e falsas carregadas na arma, sem revelar a ordem delas. O Jogador 1 recebe o primeiro turno e pode escolher entre as seguintes ações: Atirar em um dos outros jogadores (Jogador 2 ou Jogador 3) Atirar em si mesmo Resultados possíveis das ações Se o jogador atirar em outro jogador: Bala verdadeira: o alvo sofre 1 ponto de dano, e o turno passa para o próximo jogador. Bala falsa: nenhum dano é causado, e o turno do alvo é pulado, avançando diretamente para o próximo jogador da ordem. Se o jogador atirar em si mesmo: Bala verdadeira: o jogador sofre 1 ponto de dano, e o turno passa normalmente para o próximo jogador. Bala falsa: nenhum dano é causado, e o jogador mantém o turno, podendo jogar novamente. O ciclo de turnos se repete com os demais jogadores, seguindo as mesmas regras, até que reste apenas um jogador vivo, que será declarado o vencedor.
