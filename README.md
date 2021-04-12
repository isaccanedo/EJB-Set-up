## Guia para configuração EJB

# 1. Introdução
Neste artigo, vamos discutir como iniciar o desenvolvimento Enterprise JavaBean (EJB).

Os Enterprise JavaBeans são usados para desenvolver componentes escalonáveis e distribuídos do lado do servidor e normalmente encapsulam a lógica de negócios do aplicativo.

Usaremos o WildFly 10.1.0 como nossa solução de servidor preferencial; no entanto, você pode usar qualquer servidor de aplicativos Java Enterprise de sua escolha.

# 2. Configuração
Vamos começar discutindo as dependências do Maven necessárias para o desenvolvimento do EJB 3.2 e como configurar o servidor de aplicativos WildFly usando o plug-in Maven Cargo ou manualmente.

### 2.1. Dependência Maven
Para usar o EJB 3.2, certifique-se de adicionar a versão mais recente à seção de dependências do seu arquivo pom.xml:

```
<dependency>
    <groupId>javax</groupId>
    <artifactId>javaee-api</artifactId>
    <version>7.0</version>
    <scope>provided</scope>
</dependency>
```

Você encontrará a dependência mais recente no Repositório Maven. Essa dependência garante que todas as APIs Java EE 7 estejam disponíveis durante o tempo de compilação. O escopo fornecido garante que, uma vez implantado, a dependência será fornecida pelo contêiner onde foi implantado.

### 2.2. Configuração do WildFly com Maven Cargo
Vamos falar sobre como usar o plugin Maven Cargo para configurar o servidor.

Aqui está o código para o perfil Maven que provisiona o servidor WildFly:

```
<profile>
    <id>wildfly-standalone</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.cargo</groupId>
                <artifactId>cargo-maven2-plugin</artifactId>
                <version>${cargo-maven2-plugin.version</version>
                <configuration>
                    <container>
                        <containerId>wildfly10x</containerId>
                        <zipUrlInstaller>
                            <url>
                                http://download.jboss.org/
                                  wildfly/10.1.0.Final/
                                    wildfly-10.1.0.Final.zip
                            </url>
                        </zipUrlInstaller>
                    </container>
                    <configuration>
                        <properties>
                            <cargo.hostname>127.0.0.0</cargo.hostname>
                            <cargo.jboss.management-http.port>
                                9990
                            </cargo.jboss.management-http.port>
                            <cargo.servlet.users>
                                testUser:admin1234!
                            </cargo.servlet.users>
                        </properties>
                    </configuration>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

Usamos o plugin para baixar o zip WildFly 10.1 diretamente do site do WildFly. Que é então configurado, certificando-se de que o nome do host é 127.0.0.1 e definindo a porta como 9990.

Em seguida, criamos um usuário de teste, usando a propriedade cargo.servlet.users, com o id de usuário testUser e a senha admin1234!.

Agora que a configuração do plugin está concluída, devemos ser capazes de chamar um destino Maven e fazer o download do servidor, ser instalado, iniciado e o aplicativo implantado.

Para fazer isso, navegue até o diretório ejb-remote e execute o seguinte comando:


```
mvn clean package cargo:run
```

Quando você executa esse comando pela primeira vez, ele baixa o arquivo zip WildFly 10.1, extrai-o e executa a instalação e, em seguida, inicia-o. Ele também adicionará o usuário de teste discutido acima. Quaisquer outras execuções não farão o download do arquivo zip novamente.

### 2.3. Configuração manual do WildFly
Para configurar o WildFly manualmente, você deve baixar o arquivo zip de instalação no site wildfly.org. As etapas a seguir são uma visão de alto nível do processo de configuração do servidor WildFly:

Depois de baixar e descompactar o conteúdo do arquivo para o local onde deseja instalar o servidor, configure as seguintes variáveis de ambiente:

```
JBOSS_HOME=/Users/$USER/../wildfly.x.x.Final
JAVA_HOME=`/usr/libexec/java_home -v 1.8`
```

Em seguida, no diretório bin, execute ./standalone.sh para sistemas operacionais baseados em Linux ou ./standalone.bat para Windows.

Depois disso, você terá que adicionar um usuário. Este usuário será usado para se conectar ao bean EJB remoto. Para descobrir como adicionar um usuário, você deve dar uma olhada na documentação "adicionar um usuário".

Para obter instruções detalhadas de configuração, visite a documentação de primeiros passos do WildFly.

O projeto POM foi configurado para funcionar com o plugin Cargo e configuração manual do servidor, definindo dois perfis. Por padrão, o plugin Cargo está selecionado. No entanto, para implantar o aplicativo em um servidor Wildfly já instalado, configurado e em execução, execute o seguinte comando no diretório ejb-remote:

```
mvn clean install wildfly:deploy -Pwildfly-runtime
```

# 3. Remoto vs Local
Uma interface de negócios para um bean pode ser local ou remota.

Um bean anotado @Local só pode ser acessado se estiver no mesmo aplicativo que o bean que faz a chamada, ou seja, se eles residirem no mesmo .ear ou .war.

Um bean anotado @Remote pode ser acessado de um aplicativo diferente, ou seja, um aplicativo residente em um JVM ou servidor de aplicativos diferente.

Existem alguns pontos importantes a serem considerados ao projetar uma solução que inclua EJBs:

- O java.io.Serializable, java.io.Externalizable e interfaces definidas pelo pacote javax.ejb são sempre excluídos quando um bean é declarado com @Local ou @Remote;
- Se uma classe de bean for remota, todas as interfaces implementadas devem ser remotas;
-Se uma classe de bean não contiver anotação ou se a anotação @Local for especificada, todas as interfaces implementadas serão consideradas locais;
- Qualquer interface explicitamente definida para um bean que não contém interface deve ser declarada como @Local;
- A versão EJB 3.2 tende a fornecer mais granularidade para situações em que as interfaces locais e remotas precisam ser definidas explicitamente.

# 4. Criando o EJB Remoto
Vamos primeiro criar a interface do bean e chamá-la de HelloWorld:

```
@Remote
public interface HelloWorld {
    String getHelloWorld();
}
```

Agora vamos implementar a interface acima e nomear a implementação concreta HelloWorldBean:

```
@Stateless(name = "HelloWorld")
public class HelloWorldBean implements HelloWorld {

    @Resource
    private SessionContext context;

    @Override
    public String getHelloWorld() {
        return "Welcome to EJB Tutorial!";
    }
}
```

Observe a anotação @Stateless na declaração da classe. Isso denota que este bean é um bean de sessão sem estado. Esse tipo de bean não tem nenhum estado de cliente associado, mas pode preservar seu estado de instância e normalmente é usado para fazer operações independentes.

A anotação @Resource injeta o contexto da sessão no bean remoto.

A interface SessionContext fornece acesso ao contexto de sessão de tempo de execução que o contêiner fornece para uma instância de bean de sessão. O contêiner então passa a interface SessionContext para uma instância após a instância ter sido criada. O contexto da sessão permanece associado a essa instância para o seu tempo de vida.

O contêiner EJB normalmente cria um pool de objetos do bean sem estado e usa esses objetos para processar solicitações do cliente. Como resultado desse mecanismo de agrupamento, os valores das variáveis ​​de instância não têm garantia de serem mantidos nas chamadas de método de pesquisa.

# 5. Configuração Remota
Nesta seção, discutiremos como configurar o Maven para construir e executar o aplicativo no servidor.

Vejamos os plug-ins um por um.

### 5.1. O plug-in EJB
O plugin EJB fornecido a seguir é usado para empacotar um módulo EJB. Especificamos a versão EJB como 3.2.

A seguinte configuração de plug-in é usada para configurar o JAR de destino para o bean:

```
<plugin>
    <artifactId>maven-ejb-plugin</artifactId>
    <version>2.4</version>
    <configuration>
        <ejbVersion>3.2</ejbVersion>
    </configuration>
</plugin>
```

### 5.2. Implantar o EJB Remoto
Para implantar o bean em um servidor WildFly, certifique-se de que o servidor esteja ativo e em execução.

Então, para executar a configuração remota, precisaremos executar os seguintes comandos Maven no arquivo pom no projeto ejb-remote:

```
mvn clean install
```

Então devemos executar:

```
mvn wildfly:deploy
```

Como alternativa, podemos implementá-lo manualmente como um usuário administrador no console de administração do servidor de aplicativos.

# 6. Configuração do cliente
Depois de criar o bean remoto, devemos testar o bean implantado criando um cliente.

Primeiro, vamos discutir a configuração do Maven para o projeto do cliente.

6.1. Configuração do Maven do lado do cliente
Para iniciar o cliente EJB3, precisamos adicionar as seguintes dependências:

```
<dependency>
    <groupId>org.wildfly</groupId>
    <artifactId>wildfly-ejb-client-bom</artifactId>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

Dependemos das interfaces de negócios remotas EJB deste aplicativo para executar o cliente. Portanto, precisamos especificar a dependência JAR do cliente EJB. Adicionamos o seguinte no pom pai:

```
<dependency>
    <groupId>com.isaccanedo.ejb</groupId>
    <artifactId>ejb-remote</artifactId>
    <type>ejb</type>
</dependency>
```

O ```<tipo>``` é especificado como ejb.

### 6.2. Acessando o Remote Bean
Precisamos criar um arquivo em src / main / resources e nomeá-lo jboss-ejb-client.properties que conterá todas as propriedades necessárias para acessar o bean implantado:

```
remote.connections=default
remote.connection.default.host=127.0.0.1
remote.connection.default.port=8080
remote.connection.default.connect.options.org.xnio.Options
  .SASL_POLICY_NOANONYMOUS = false
remote.connection.default.connect.options.org.xnio.Options
  .SASL_POLICY_NOPLAINTEXT = false
remote.connection.default.connect.options.org.xnio.Options
  .SASL_DISALLOWED_MECHANISMS = ${host.auth:JBOSS-LOCAL-USER}
remote.connection.default.username=testUser
remote.connection.default.password=admin1234!
```

# 7. Criação do cliente
A classe que irá acessar e usar o bean HelloWorld remoto foi criada em EJBClient.java que está no pacote com.isaccanedo.ejb.client.

# 7.1 URL do bean remoto
O bean remoto está localizado por meio de um URL que está em conformidade com o seguinte formato:

```
ejb:${appName}/${moduleName}/${distinctName}/${beanName}!${viewClassName}
```

- O $ {appName} é o nome do aplicativo da implantação. Aqui, não usamos nenhum arquivo EAR, mas uma implementação simples de JAR ou WAR, portanto, o nome do aplicativo estará vazio;
- O $ {moduleName} é o nome que definimos para nossa implantação anterior, portanto, é ejb-remote;
- O $ {DifferentName} é um nome específico que pode ser opcionalmente atribuído às implantações que são implantadas no servidor. Se uma implantação não usa nome distinto, podemos usar uma String vazia no nome JNDI, para o nome diferente, como fizemos em nosso exemplo;
- A variável $ {beanName} é o nome simples da classe de implementação do EJB, então em nosso exemplo é HelloWorld;
- $ {viewClassName} denota o nome de interface totalmente qualificado da interface remota.

### 7.2 Look-up Logic
A seguir, vamos dar uma olhada em nossa lógica de pesquisa simples:

```
public HelloWorld lookup() throws NamingException { 
    String appName = ""; 
    String moduleName = "remote"; 
    String distinctName = ""; 
    String beanName = "HelloWorld"; 
    String viewClassName = HelloWorld.class.getName();
    String toLookup = String.format("ejb:%s/%s/%s/%s!%s",
      appName, moduleName, distinctName, beanName, viewClassName);
    return (HelloWorld) context.lookup(toLookup);
}
```

Para conectar ao bean que acabamos de criar, precisaremos de uma URL que podemos inserir no contexto.

### 7.3 O Contexto Inicial
Agora vamos criar / inicializar o contexto da sessão:

```
public void createInitialContext() throws NamingException {
    Properties prop = new Properties();
    prop.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
    prop.put(Context.INITIAL_CONTEXT_FACTORY, 
      "org.jboss.naming.remote.client.InitialContextFacto[ERROR]
    prop.put(Context.PROVIDER_URL, "http-remoting://127.0.0.1:8080");
    prop.put(Context.SECURITY_PRINCIPAL, "testUser");
    prop.put(Context.SECURITY_CREDENTIALS, "admin1234!");
    prop.put("jboss.naming.client.ejb.context", false);
    context = new InitialContext(prop);
}
```

Para conectar ao bean remoto, precisamos de um contexto JNDI. A fábrica de contexto é fornecida pelo artefato Maven org.jboss: jboss-remote-naming e isso cria um contexto JNDI, que resolverá a URL construída no método de pesquisa, em proxies para o processo do servidor de aplicativo remoto.

### 7.4 Definir parâmetros de pesquisa
Definimos a classe de fábrica com o parâmetro Context.INITIAL_CONTEXT_FACTORY.

O Context.URL_PKG_PREFIXES é usado para definir um pacote para verificar o contexto de nomenclatura adicional.

O parâmetro org.jboss.ejb.client.scoped.context = false informa ao contexto para ler os parâmetros de conexão (como o host de conexão e a porta) do mapa fornecido em vez de um arquivo de configuração do classpath. Isso é especialmente útil se quisermos criar um pacote JAR que deve ser capaz de se conectar a hosts diferentes.

O parâmetro Context.PROVIDER_URL define o esquema de conexão e deve começar com http-remoting://.

# 8. Teste
Para testar a implantação e verificar a configuração, podemos executar o seguinte teste para garantir que tudo funcione corretamente:

```
@Test
public void testEJBClient() {
    EJBClient ejbClient = new EJBClient();
    HelloWorldBean bean = new HelloWorldBean();
    
    assertEquals(bean.getHelloWorld(), ejbClient.getEJBRemoteMessage());
}
```

Com a aprovação no teste, agora podemos ter certeza de que tudo está funcionando conforme o esperado.

# 9. Conclusão
Portanto, criamos um servidor EJB e um cliente que invoca um método em um EJB remoto. O projeto pode ser executado em qualquer servidor de aplicativos, incluindo adequadamente as dependências desse servidor.