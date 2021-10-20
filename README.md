## Kafka-streams demonstração

Essa demontração foi desenvolvida com o intuíto de demonstrar algumas funcionalidades da biblioteca de Kafka Streams. Portanto, eu divi a demo em 3 partes: Um publicador, uma aplicação de Kafka Streams e o próprio Kafka em conjunto com o Zookeeper.

### The demonstration

<div style="text-align:center"><img src="./misc/images/demonstration-high-level.png" alt="demonstration high level"/></div>

#### Common definitions:
    Um boleto bancário (ou simplesmente boleto ou, ainda, bloqueto) é um documento largamente utilizado no Brasil como instrumento de pagamento de um produto ou serviço prestado. Através do boleto, seu emissor pode receber do pagador o valor referente àquele pagamento. - Wikipedia

O objetivo da demonstração é criar um cenário onde a aplicação de Streams recebe um boleto para validar de um tópico kafka ("left-topic"), inicia um processo e encaminha esse boleto para uma outra aplicação para completar a validação. A aplicação seguinte processa a validação e retorna o resultado em um tópico Kafka distinto ("right-topic"). A aplicação de Streams mencionada anteriormente consome esse tópico também.

Existe uma janela operacional de 30 minutos para validar um boleto e a aplicação streams é responsável por produzir a resposta. Então, quando a validação de um boleto é recebida dentro de 30 minutos, a aplicação de Streams produz um resultado em outro tópico Kafka ("output-topic"). Caso a segunda aplicação não produza a validação para a aplicação de Streams dentro de 30 minutos, a aplicação de Streams deve produzir um outro evento no tópico Kafka ("output-topic"), avisando que o boleto não pode ser validado a tempo.

A aplicação de Streams fará uso da biblioteca oficial de Kafka Streams para implementar esse comportamento. A aplicação de Streams criará duas abstrações de streams (KStream) consumindo simultâneamente dois tópicos Kafka mencionandos anteriormente ("left-topic" e "right-topic").

Para a stream consumindo do tópico Kafka "left-topic", a aplicação iniciará um processamento stateful mantendo todos os boletos com validação pendente em uma local store. A aplicação de streams possui um mecânismo interno de timeout que remove todos os boletos com validação pendente após 30 minutos. Para cada um dos boletos removidos, a aplicação de streams envia um evento correspondente avisando no tópico Kafka "output-topic". Quando a outra stream que consome do tópico Kafka "right-topic" recebe um novo evento (validação), nossa aplicação de streams verifica se o boleto correspondente ainda está aguardando validação da local store. Se esse for o caso, nós enviamos um evento de validação esse boleto no tópico Kafka "output-topic" e removemos o mesmo da local store. Se a aplicação de streams já removeu o boleto da local store, enviamos um evento de validação atrasada no tópico Kafka "output-topic".

### Kafka Publisher

<div style="text-align:center"><img src="./misc/images/kafka-publisher.png" alt="kafka-publisher"/></div>

Para testar esse cenário precisamos ser capazes de gerar dados fake para boletos e suas respectivas validações. A aplicação Kafka Publisher é responsável por gerar esses dados e inserir os mesmo nos tópicos Kafka ("left-topic" e "right-topic").

#### ENV VARIABLES

    - BOOTSTRAP_SERVERS -> Lista separada por vírgula contendo os hosts e as respectivas portas dos brokers kafka.
    - LEFT_TOPIC -> Nome do tópico usado para publicar boletos
    - RIGHT_TOPIC -> Nome do tópico usado para publicar validações
    - CLIENT_ID_CONFIG_BOLETO -> Um ID repassado para o servidor enquanto requisições são feitas. O propósito desse ID é rastrear a origem para além de simplismente IP/Porta permitindo um nome de aplicação nos logs. Usado pelo producer de boletos.
    - CLIENT_ID_CONFIG_BOLETO_VALIDATION -> Um ID repassado para o servidor enquanto requisições são feitas. O propósito desse ID é rastrear a origem para além de simplismente IP/Porta permitindo um nome de aplicação nos logs. Usado pelo producer de validações.

### Kafka Streams Application

<div style="text-align:center"><img src="./misc/images/kafka-streams.png" alt="kafka-streams"/></div>

A aplicação de streams terá toda a funcionalidade descrita anteriormente. Ela consumirá ambos os tópicos Kafka ("left-topic" e "right-topic") e dentro de no máximo 30 minutos produzirá um resultado.

#### Kafka Streams Topology

<div style="text-align:center"><img src="./misc/images/kafka-streams-topology.png" alt="kafka-streams-topology"/></div>

Kafka Streams utiliza um paradigma de programação denominado "dataflow programming" (DFP), um método centralizado em dados representando uma série de entradas, saídas e estágios de processamento. A lógica de processamento de fluxo de dados em uma aplicação de Kafka Streams é estruturada como um grafo aciclico (DAG), onde nós (os retângulos no diagrama acima) representam um processador/Processor. As conexões (linhas ligando os processadores/Processors) representam a entrada e saída de fluxos de dados. Portanto uma coleção de processadore/Processor foram uma topologia de processamento (Processor Topology) para uma aplicação de Kafka Streams.

#### ENV VARIABLES

    - BOOTSTRAP_SERVERS -> Lista separada por vírgula contendo os hosts e as respectivas portas dos brokers kafka.
    - LEFT_TOPIC -> Nome do tópico usado para consumir boletos
    - RIGHT_TOPIC -> Nome do tópico usado para consumir validações
    - OUTPUT_TOPIC -> Nome do tópico usado para publicar resultados
    - STORE_NAME -> Nome utilizado na store local
    - CLIENT_ID_CONFIG -> Um ID repassado para o servidor enquanto requisições são feitas. O propósito desse ID é rastrear a origem para além de simplismente IP/Porta permitindo um nome de aplicação nos logs.
    - APPLICATION_ID_CONFIG -> Deve ser único dentro de todo o cluster Kafka porque é utilizado como namespace para o padrão de prefixo de client-id, group-id para gerencimaneto de membros e prefixo para tópicos internos criados pela biblioteca de Kafka Streams.
    - COMMIT_INTERVAL_MS_CONFIG -> Frequência em que a aplicação realiza commits da posição dos Processors.

### Kafka and Zookeeper

<div style="text-align:center"><img src="./misc/images/kafka-zookeeper.png" alt="kafka-zookeeper"/></div>

As aplicações de Kafka Publisher e Streams precisam de um cluster Kafka e um Zookeeper para poderem se comunidar. 

### Pré-requisitos (já instalados na instância criada pelo Template CloudFormation)
    - Docker
    - Docker-compose

### Como rodar
**Você não precisa configurar nenhuma variável de ambiente dado que as mesmas possuem valores padrão**

Crie uma nova stack no serviço AWS CloudFormation com o template presente na pasta aws/ na raiz do repositório.

Conecte na instância EC2 criada via AWS Systems Manager.

    - Aguarde alguns minutos enquanto o Docker faz build da aplicação kafka-streams-app e da aplicação kafka-publisher
    - docker run --network=host kafka-publisher
    - docker run --network=host kafka-streams-app

Toda vez que você desejar gear mais dados, você precisa executar o kafka-publisher.