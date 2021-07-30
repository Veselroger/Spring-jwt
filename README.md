# <a id="home"></a> Spring JWT secure

**Table of Contents:**
- [Maven project](#maven)
- [Domain model](#model)
- [Rest Application](#rest)
- [Data Source (PostgreSQL)](#datasource)
- [Spring Data JPA](#jpa)
- [HTTPS](#https)
- [JSON Web Token](#jwt)
- [Spring Security + JWT](#security)
- [PasswordEncoder & SignUp](#signup)
- [GrantedAuthority](#roles)
- [Resources](#resources)

----

## [↑](#home) <a id="maven"></a> Maven project
Для создания проекта воспользуемся системой сборки **[Maven](https://maven.apache.org/)**. Для этого мы должны описать проект при помощи так называемой **[Project Object Model](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html)**. Модель проекта описывается в файле **pom.xml**.

Минимальный pom файл должен содержать **[Maven coordinates](https://maven.apache.org/pom.html#Maven_Coordinates)** проекта и **modelVersion**, которая отражает версию языка описания Maven проектов. Наш вариант:
```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.github.veselroger</groupId>
    <artifactId>jwtsecure</artifactId>
    <version>1.0-SNAPSHOT</version>
</project>
```
По всем правилам версия нашего проекта должна быть **[SNAPSHOT](https://maven.apache.org/guides/getting-started/index.html#What_is_a_SNAPSHOT_version)**. 

Кроме того, необходимо создать структуру каталогов для проекта. У Maven есть своя структура проектов: **"[Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html)"**. 
- ``src/main/java`` содержит java классы
- ``src/main/resources`` содержит ресурсы (например, настройки)

Создать их можно при помощи любого терминала, например git bash:
```cmd
mkdir -p ./src/main/java
mkdir -p ./src/main/resources
```

Если не хочется это делать вручную, то можно создать Maven проект через IDE или воспользоваться генерацией из **[Maven Archetype](https://maven.apache.org/guides/introduction/introduction-to-archetypes.html)**:
> mvn archetype:generate

В этом случае для всего, что не относится к Maven coordinates нашего проекта просто нажимаем Enter чтобы выбрать значение по умолчанию.

В pom.xml необходимо указать версию Java, под которую мы будем компилировать наш проект:
```xml
<properties>
    <maven.compiler.release>14</maven.compiler.release>
</properties>
```

Остаётся только проверить (провалидировать), что мы всё написали верно. Можно воспользоваться самой первой фазой из **"[Maven Default Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)"**:
```
mvn validate
```

Кроме того, можно сделать работу с pom.xml файлом немного приятнее для IntelliJ Idea. Для этого можно поставить плагин **[Laconic POM](https://plugins.jetbrains.com/plugin/10580-laconic-pom)**.

----------------

## [↑](#home) <a id="domain"></a> Domain Model
Начнём с того, что опишем некоторые термины (домены) нашей системы.
У нас есть пользователь и роли.
Идею можно подсмотреть в **"[PostgreSQL Tutorial](https://www.postgresqltutorial.com/postgresql-create-table/)"**.

Исторически так сложилось, что Java временами требует boilerplate код, например getter'ы и setter'ы для полей. Чтобы немного улучшить ситуацию подключим библиотеку **[lombok](https://projectlombok.org/setup/maven)**:
```xml
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.20</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```
Lombok при компиляции будет "дописывать" boilerplate код за нас. Чтобы IDE понимал это нужно включить опцию "enable annotation processing". В IntelliJ Idea эта опция находистя в Settings, в разделе "Annotation processors". Кроме того обязательно надо установить **[IntelliJ Idea Lombok Plugin](https://plugins.jetbrains.com/plugin/6317-lombok)**.

Создадим пакет, в котором будем располагать все наши классы и подпакеты, т.к. Spring не рекомендует использовать default package, о чём подробнее можно прочитать в документации Spring: **"[Using the default Package](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.structuring-your-code.using-the-default-package)"**. Например:
```cmd
mkdir -p ./src/main/java/com/github/veselroger/jwt
```

Создадим в этом пакете подпакет **model** и создадим там пока что два класса. Класс User будет представлять пользователя системы:
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;
}
```
Класс Role будет представлять роль пользователя в системе:
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    private Long id;
    private String name;
}
```
Теперь, напишем заготовку для REST сервиса, который будет использовать эту модель.

----------------

## [↑](#home) <a id="rest"></a> Spring Rest Application
Создадим Spring REST приложение. Для этого подключим к проекту **"[Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/getting-started.html#getting-started.introducing-spring-boot)"**.

Начнём с импорта **[Bills of Materials](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.build-systems.dependency-management)**. BoM рассказывает проекту, какие версии для каких библиотек нужно использовать, если эти библиотеки кто-то захочет подключить к проекту. Сделаем так:
```xml
<dependencyManagement>
	<dependencies>
		<dependency>
			<!-- https://docs.spring.io/spring-boot/docs/2.5.3/maven-plugin/reference/htmlsingle/#using.import -->
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-dependencies</artifactId>
			<version>2.5.3</version>
			<type>pom</type>
			<scope>import</scope>
		</dependency>
	</dependencies>
</dependencyManagement>
```
Стоит обратить внимание так же, что мы использовали **[import scope](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Importing_Dependencies)**.

Теперь подключим к проекту сам функционал Spring Boot. Функционал Spring Boot разбит на различные модули и подключается при помощи **стартеров**. Подробнее см. в документации Spring Boot: **"[1.5. Starters](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.build-systems.starters)"**.

В документации к стартерам написано, что если нам нужен REST, то нужно подключить **spring-boot-starter-web**:
```xml
<!-- Starters list: https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.build-systems.starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
Благодаря объявленному ранее dependency management'у мы можем не указывать версии, Maven их возьмёт из BoM сам.

Создадим в основном пакете главный класс приложения согласно документации Spring Boot: **"[Locating the Main Application Class](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.structuring-your-code.using-the-default-package)"**:
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Создадим отдельный пакет **controllers**. Там будут находится **контроллеры**, которые будут отвечать на входящие запросы про пользователей нашей системы. Подробнее про **@Controller**'ы для REST сервисов можно прочитать в документации: **"[1.3. Annotated Controllers](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-controller)"**. 

Так как мы хотим возвращать в качестве ответа JSON, то воспользуемся специальной аннотацией **@RestController**, которая включает в себя @Controller и @ResponseBody. Благодаря @ResponseBody возвращаемый методом объект будет преобразован при помощи HttpMessageConverter в нужный вид.

При помощи **@RequestMapping** выполним привязку запросов. А при помощи **@PathVairable** в запросе будем использовать переменную:
```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    User getUser(@PathVariable Long id) {
        return new User(id, "User #" + id, "user@test.com");
    }
}
```

Теперь, если мы запустим приложение, то сможем получить указанный ответ, если выполним запрос ``http://127.0.0.1:8080/users/1``. 

Например, можно в git bash использовать **[curl](https://curl.se/)**:
> curl http://127.0.0.1:8080/users/1 -s

Сам REST сервис работает. Теперь стоит подключить настоящий источник данных, в котором будут храниться пользователи и другие данные.

------------

## [↑](#home) <a id="datasource"></a> Data Source (PostgreSQL)
Создадим источник данных (**Data Source**), в котором мы будем хранить все наши данные о пользователях, их ролях и т.п.

Для большей реалистичности мы можем воспользоваться сервисом **PostgreSQL as a Service** под названием **[ElephantSQL](https://www.elephantsql.com)**.
После регистрации и входа на этот сервис выбираем **"Create New Instance"** для создания нового экземпляра БД. В разделе details нашего instance будет доступна информация для подключения.

Создадим в нашей базе данных таблицу для пользователей и создадим там пару записей. Например, при помощи раздела "Browser" в UI сервиса elephantsql.com мы можем выполнить запросы. Оставим схему БД по умолчанию, т.е. схему **[public](https://postgrespro.ru/docs/postgresql/13/ddl-schemas#DDL-SCHEMAS-PUBLIC)**. 

Создадим таблицу по аналогии с примером из "**[PostgreSQL Tutorial: CREATE TABLE](https://www.postgresqltutorial.com/postgresql-create-table/)**":
```sql
CREATE TABLE accounts (
	user_id serial PRIMARY KEY,
	username VARCHAR ( 50 ) UNIQUE NOT NULL,
	password VARCHAR ( 50 ) NOT NULL,
	email VARCHAR ( 255 ) UNIQUE NOT NULL
);
```
Индексы нам создавать не надо, т.к. одно из ключевых для поиска полей PK, а другое - UNIQUE. Следовательно, PostgreSQL автоматически для них создаст индексы.

Создадим таблицу для ролей:
```sql
CREATE TABLE roles(
   role_id serial PRIMARY KEY,
   role_name VARCHAR (50) UNIQUE NOT NULL
);
```

А так же нам понадобится таблица для связи пользователей и ролей:
```sql
CREATE TABLE account_roles (
  user_id INT NOT NULL,
  role_id INT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (role_id)
      REFERENCES roles (role_id),
  FOREIGN KEY (user_id)
      REFERENCES accounts (user_id)
);
```

И добавим первого пользователя (хранение пароля исправим позже):
```sql
INSERT INTO accounts(username, password, email)
VALUES ('admin', 'admin', 'admin@example.com');
```

Предусмотрим возможность блокировать/отключать пользователей. Для этого **[добавим новый столбец](https://www.postgresqltutorial.com/postgresql-add-column/)** с именем "disabled":
```sql
ALTER TABLE accounts
ADD COLUMN disabled BOOLEAN;
```

Data Source готов. Самое время научить наше приложение с ним работать.

------------

## [↑](#home) <a id="jpa"></a> Spring Data JPA
Подключим к нашему REST сервису источник данных. Для этого нам понадобится подключить JDBC драйвер к проекту в соответствии с тем, какую базу данных мы выберем. Т.к. мы ранее выбрали PostgreSQL, то нужен нам **[PostgreSQL JDBC драйвер](https://jdbc.postgresql.org/download.html)**:
```xml
<!-- https://mvnrepository.com/artifact/org.postgresql/postgresql -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.2.23</version>
</dependency>
```

Теперь настроим подключение к БД. Одна из возможностей Spring Boot - это **"[Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)"**. Мы можем вынести настройки в отдельный файл конфигурации. Более того, в этом файле можно использовать переменные среды окружения, т.к. одно из требований безопасности - никогда не хранить чувствительные данные (логины/пароли) в исходном коде.

Создадим в каталоге ресурсов файл конфигурации ``application.yml``:
```yml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://chunee.db.elephantsql.com/emrsigxl
    username: ${db.username}
    password: ${db.password}
```
В IntelliJ Idea в **"[Run -> Edit Configurations](https://www.jetbrains.com/help/idea/run-debug-configuration.html)"** можно указать значения для этих placeholder'ов в секции **Environment Variables**.

Далее подключим к проекту при помощи стартера **Spring Data JPA**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```
Теперь нужно указать некоторые настройки, которые относятся к Hibernate, который является реализацией JPA по умолчанию. Например, указание настройки [HBM2DDL](https://docs.jboss.org/hibernate/orm/5.5/userguide/html_single/Hibernate_User_Guide.html#configurations-hbmddl):
```yml
jpa:
    show-sql: true
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

#printing parameter values in order
logging.level.org.hibernate.type.descriptor.sql: trace
```

Теперь на стороне приложения нужно соотнести ("смапить") наш Java класс на таблицу из БД. Подробнее про это можно прочитать в документации Hibernate: **"[2.1. Mapping types](https://docs.jboss.org/hibernate/orm/5.5/userguide/html_single/Hibernate_User_Guide.html#mapping-types)"**. Модифицируем класс пользователя:
```java
@Data
@AllArgsConstructor
@NoArgsConstructor // For Hibernate
@Entity(name = "accounts")
public class User {
    @Id
    @Column(name = "user_id")
    private Long id;
    @Column(name = "username")
    private String name;
    @Column(name = "password")
    private String password;
    @Column(name = "email")
    private String email;
    @Column(name = "disabled")
    private Boolean disabled;
}
```

Аналогично дополним роли:
```java
@Data
@AllArgsConstructor
@NoArgsConstructor // For Hibernate
@Entity(name = "roles")
public class Role {
    @Id
    @Column(name = "role_id")
    private Long id;
    @Column(name = "role_name")
    private String name;
}

```

Осталось подружить User с его ролями. У нас есть два варианта:
- **One-to-Many**
User имеет несколько ролей, но роли не переиспользуются пользователями (т.е. у каждого User своя роль "admin")
- **Many-to-Many**
User имеет несколько ролей, а роли переиспользуются между пользователями (т.е. у нескольких User одинаковая роль "admin")

Очевидно, что у нас будет связь **[Many-to-Many](https://docs.jboss.org/hibernate/orm/5.5/userguide/html_single/Hibernate_User_Guide.html#associations-many-to-many)**. Когда у нас есть коллекции (у нас это коллекция ролей), то стоит помнить что при управлении коллекциями в Hibernate есть две стороны: **owning side** (есть всегда) и **inverse (mappedBy) side** (при двунаправленной связи). Подробнее см. документацию Hibernate: **"[Collections of entities](https://docs.jboss.org/hibernate/orm/5.5/userguide/html_single/Hibernate_User_Guide.html#collections-entity)"**.

Добавим маппинг в User:
```java
@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
@JoinTable(name = "account_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
)
private List<Role> roles;
```
Т.к. роли нам нужны не всегда, то делаем маппинг Lazy.

Более того, мы уже сталкиваемся с тем, что у нас есть Entity, а есть возвращаемый пользователю результат. Считается дурным тоном возвращать Entity в ответ на REST запрос. Подробнее см. **"[Thorben Janssen: Don’t expose your JPA entities in your REST API](https://thorben-janssen.com/dont-expose-entities-in-api/)"**.

Создадим в подпакете dto свой **[DTO](https://thorben-janssen.com/dto-projections/)** для возвращения в качестве ответа:
```java
@Data
public class UserDto {
    private Long id;
    private String name;
    
    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
    }
}
```

Теперь у нас есть entity. Нам теперь нужно создать Spring Data JPA репозиторий, который будет уметь через entity работать с БД. Создадим пакет **repositories** и создадим там репозиторий для пользователей:
```java
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findByName(String name);
}
```
Подробнее про CrudRepository можно прочитать в документации Spring: **"[Working with Spring Data Repositories](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories)"**.

Теперь нам нужен Service Layer. Создадим для него пакет **services**. А в пакете создадим сервис для пользователей:
```java
@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(String name) {
        return userRepository.findByName(name);
    }
}
```

И финальный штрих - наш REST контроллер должен теперь искать данные о пользователях в БД и возвращать DTO. Подправим код контроллера:
```java
@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{name}")
    UserDto getUser(@PathVariable String name) {
        return new UserDto(userService.getUser(name));
    }
}
```
Мы добавили конструктор, чтобы Spring благодаря Constructor Injection предоставил нам ссылку на готовый к работе UserService. Кроме того, мы теперь ищем пользователей не по id, а по имени (что более привычно).

Например, можно в git bash использовать **[curl](https://curl.se/)**:
> curl http://127.0.0.1:8080/users/admin -s

Теперь наш сервис возвращает данные из базы данных. Однако, запросы к серверу выполняем по HTTP, что небезопасно. Поэтому наша следующая цель - включить поддержку HTTPS.

------------

## [↑](#home) <a id="https"></a> HTTPS
Чтобы избежать проблемы **[Man in the middle](https://www.youtube.com/watch?v=-rSqbgI7oZM)** необходимо обезопасить общение с сервером. Для этого нам нужно использовать протокол HTTPS.

Стоит освежить своё понимание того, как работает HTTPS:
- [Андрей Созыкин: Установка соединения в TLS](https://www.youtube.com/watch?v=lKfyM980cOw&list=PLtPJ9lKvJ4oiFnWCsVRElorOLt69YDEnv&index=6)
- [Public key cryptography - Diffie-Hellman Key Exchange](https://www.youtube.com/watch?v=YEBfamv-_do)
- [Шифрование Диффи-Хеллман](https://www.youtube.com/watch?v=P1wwH8RCNgk)

Нам нужно подготовить сертификат для обращения к нашему серверу по HTTPS. Для конспекта подойдёт и самоподписанный сертификат. И для его создания нам понадобится утилита keytools.

Так как мы используем Maven, то мы можем упростить себе задачу и добавить специальный плагин, который бы нам создавал сертификат:
```xml
<build>
	<plugins>
		<plugin>
			<groupId>org.codehaus.mojo</groupId>
			<artifactId>keytool-maven-plugin</artifactId>
			<version>1.5</version>
			<executions>
			  <execution>
				<goals>
				  <goal>generateKeyPair</goal>
				</goals>
				<phase>generate-resources</phase>
			  </execution>
			</executions>
			<configuration>
			  <keystore>src/main/resources/keystore.p12</keystore>
			  <storepass>${key.password}</storepass>
			  <keypass>${key.password}</keypass>
			  <alias>jwt</alias>
			  <dname>CN=localhost,OU=IT,O=veselroger,L=SaintPetersburg,C=RU,email=contact@email.com</dname>
			  <sigalg>SHA256withRSA</sigalg>
			  <ext>san:critical=dns:localhost,ip:127.0.0.1" -ext bc=ca:false</ext>
			  <validity>100</validity>
			  <keyalg>RSA</keyalg>
			  <keysize>4096</keysize>
			  <verbose>true</verbose>
			</configuration>
		</plugin>
	</plugins>
</build>
```
Чтобы IDE не ругалась на key.password добавим в блок properties.

Для Self-signed Certificates используем следующие расширения:
1. -ext san:critical=dns:localhost,ip:127.0.0.1\
для выполнения subject matching по SubjectAlternativeName
2. -ext bc=ca:false\
Указание, что сертификат не для подписи других сертификатов

Теперь для генерации сертификата достаточно выполнить команду:
```
mvn keytool:generateKeyPair -Dkey.password=123456
```

Расскажем про сертификат Spring'у при помощи ``application.yml``:
```yml
server:
  port: 8043
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${key.password}
    key-store-type: pkcs12
    key-alias: jwt
    key-password: ${key.password}
```
Важно не хранить пароли в открытом виде, а подставлять их через placeholder'ы. Подробнее можно прочитать в документации Spring: **"[Property Placeholders](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.external-config.files.property-placeholders)"**. Поэтому важно не забыть добавить пароль, который мы указали в **key.password** в секцию environment variables.

Теперь перезапустим сервер и проверим, как это работает. Обратим внимание, что порты для HTTP и HTTPS отличаются (новый port - 8043). Так как мы использовали Self Signed сертификат, то его нету в реестре доверенных сертфикатов. Чтобы согласится использовать такой сертификат мы должны согласиться на режим **"[insecure](https://curl.se/docs/manpage.html#-k)"**.
Для начала убедимся, что HTTP недоступен:
> curl http://127.0.0.1:8080/users/admin -s -S -k

Теперь проверим, что HTTPS доступен:
> curl https://127.0.0.1:8043/users/admin -s -S -k

Теперь наш REST сервис работает по HTTPS и мы можем добавить аутентификацию и авторизацию через JWT токены.

------------

## [↑](#home) <a id="jwt"></a> JSON Web Token
**[JWT](https://jwt.io/introduction)** расшифровывается как JSON Web Token. Это JSON объект для безопасной передачи информации между двумя участниками. При помощи JWT токена можно один раз его получив использовать его, чтобы не нагружать каждый раз сервер проверкой учётных данных пользователей.

JWT состоит из заголовка (header), полезных данных (payload) и подписи (signature).

Важно, что header содержит информацию о том, какой алгоритм будет использован для создании подписи. Пример header'а:
```json
{"alg": "HS256", "typ": "JWT"}
```

В payload содержится вся полезная нагрузка. Данные из payload'а называют **JWT-claims**. Эти claims могут быть какими угодно, а их имена рекомендуется задавать тремя символами. Однако, claims должны иметь уникальные имена. Список claim предлагаемых JWT: **"[Registered Claim Names](https://datatracker.ietf.org/doc/html/rfc7519#section-4.1)"**. Пример payload:
```json
{"sub": "1234567890", "name": "John Doe", "admin": true}
```

Чтобы сформировать signature необходимо подготовить unsignedToken строку. Она состоит из закодированных при помощи base64 header и payload, разделённых точкой. Например:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9
```
Как мы видим, используется base64, а следовательно вся эта информация легко может быть раскодирована. По этой причине нельзя ни в коем случае здесь хранить что-то чувствительное, например пароли. 

Далее остаётся только взять строку unsignedToken и с импользованием алгоритма, указанного в alg, получить хэш по некоторому секретному ключу (**jwt secret**). Смысл подписи в том, чтобы проверять, что никто JWT токен не подменил, точнее никто информацию в payload не заменил на свою. Иначе хэш не сойдётся.

Конечный JWT tokken выглядит как объединённые через точку закодированных в base64 header, payload И signature.

Для авторизации рекомендуется использовать заголовок запроса. Рекомендуется использовать **[Bearer schema](https://learning.postman.com/docs/sending-requests/authorization/#bearer-token)**:
```
Authorization: Bearer <token>
```

Т.к. мы будем использовать JWT для наших REST запросов, то нам понадобится новая зависимость:
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
```

Как мы помним, для формирования signature нам понадобится секретный ключ. Добавим его в ``application.yml`` и в Environment Variables: 
```yml
jwt:
  secret: ${jwt.secret}
```

Теперь создадим сервис, который будет работать с JWT Token:
```java
@Service
@RequiredArgsConstructor
public class JwtService {
    private final String jwtIssuer = "veselroger";
    private org.slf4j.Logger logger;
    
    @Value("${jwt.secret}")
    private String jwtSecret;
}
```

Сервис должен уметь создавать токен для пользователя:
```java
public String generateAccessToken(User user) {
    return Jwts.builder()
            .setSubject(String.format("%s,%s", user.getId(), user.getName()))
            .setIssuer(jwtIssuer)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 1 week
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
}
```

Сервис должен уметь извлекать из токена пользователя:
```java
public UserDto getUser(String token) {
    Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
    String[] parts = claims.getSubject().split(",");
    UserDto user = new UserDto(Long.valueOf(parts[0]), parts[1]);
    return user;
}
```

Сервис должен уметь проверять время истечения токена:
```java
public Date getTokenExpirationDate(String token) {
    Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
    return claims.getExpiration();
}
```

Ну и мы должны проверить, что токен вообще читается:
```java
public boolean validate(String token) {
    try {
        Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
        return true;
    } catch (SignatureException ex) {
        logger.error("Invalid JWT signature - {}", ex.getMessage());
    } catch (MalformedJwtException ex) {
        logger.error("Invalid JWT token - {}", ex.getMessage());
    } catch (ExpiredJwtException ex) {
        logger.error("Expired JWT token - {}", ex.getMessage());
    } catch (UnsupportedJwtException ex) {
        logger.error("Unsupported JWT token - {}", ex.getMessage());
    } catch (IllegalArgumentException ex) {
        logger.error("Claims string is empty - {}", ex.getMessage());
    }
    return false;
}
```
Пример был взят отсюда: **"[Yoh0xFF/java-spring-security-example](https://github.com/Yoh0xFF/java-spring-security-example/blob/master/src/main/java/io/example/configuration/security/JwtTokenUtil.java)"**.

Получается, что инфраструктура для JWT готова. Самое время всё это включить в механизм безопасности Spring.

------------

## [↑](#home) <a id="security"></a> Spring Security + JWT
JWT настроен и теперь нужно настроить механизмы безопасности Spring. Для этого нам понадобится подключить новый стартер:
```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Прежде всего нам нужны права (роли пользователя). Права в Spring Security представлены интерфейсом **GrantedAuthority**. Ранее мы уже добавили класс для представления ролей. Необходимо наделить его свойством "является GrantedAuthority":
```java
public class Role implements GrantedAuthority {
```

А метод в Role, который требуется реализовать, опишем так:
```java
@Column(name = "role_name")
private String name;

@Override
public String getAuthority() {
    return this.name;
}
```

Далее нам нужна реализация **UserDetails**. Создадим пакет **security** и создадим там свой UserDetailsImpl:
```java
public class UserDetailsImpl implements UserDetails {
    @Getter
    private User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }
}
```

Теперь наш UserDetailsImpl может делегировать User получение данных:
```java
@Override
public String getPassword() { return user.getPassword(); }

@Override
public String getUsername() { return user.getName(); }

@Override
public boolean isAccountNonLocked() { return isEnabled(); }

@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getRoles();
}

@Override
public boolean isEnabled() {
    return user.getDisabled() == null || !user.getDisabled();
}
```

И ещё Spring требует от нас сервис, который умеет возвращать UserDetails. Создадим такой:
```java
@Data
@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        User user = userRepository.findByName(name);
        // Fetch lazy collection while we have a transaction
        user.getRoles().size();
        return new UserDetailsImpl(user);
    }
}
```
Здесь интересный момент. Где-то дальше в коде UserDetails будет использоваться в том числе для получения ролей пользователя. А роли - это ленивая коллекция, которую можно получить только в то время, как открыта сессия (т.е. есть активная транзакция), а иначе мы получим ошибку - **"Hibernate could not initialize proxy - no Session"**. Кроме того, далее мы создаём UserDetailsImpl вручную, т.е. Spring никак не отслеживает вызов методов на этом экземпляре, т.е. когда нужно не предоставит транзакцию. Поэтому, тут мы поступили самым простым способом - пока у нас есть транзакция мы вызвали метод чтения коллекции. Существуют и другие способы, о чём можно прочитать в замечательном материале от Thorben Janssen: "**[5 ways to initialize lazy associations and when to use them](https://thorben-janssen.com/5-ways-to-initialize-lazy-relations-and-when-to-use-them/)**".

Интересно, но мы можем без труда проверить, а находимся ли мы сейчас в транзакции:
```java
if (!TransactionSynchronizationManager.isActualTransactionActive()) {
    throw new IllegalStateException("Transaction is not active!");
}
```

Наша цель - обрабатывать каждый входящий запрос на предмет JWT токена. Для этих целей Spring предоставляет класс **OncePerRequestFilter**. Создадим на основе него собственный фильтр в подпакете security:
```java
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtService jwtTokenUtil;
    private final UserDetailsService userDetailsService;
}
```

И опишем сам фильтр:
```java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    // Get authorization header and validate
    final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith("Bearer ")) {
        final String jwtToken = header.split(" ")[1].trim();
        if (jwtTokenUtil.validate(jwtToken)) {
            // Get user identity and set it on the spring security context
            UserDto jwtUser = jwtTokenUtil.getUser(jwtToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(jwtUser.getName());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);
            return;
        }
    }
    // Can't authenticate with JWT token
    chain.doFilter(request, response);
}
```

Теперь нам нужно описать саму конфигурацию безопасности Spring. Делается это при помощи создания наследника от **WebSecurityConfigurerAdapter**. Создадим его в нашем подпакете security:
```java
@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private JwtTokenFilter jwtTokenFilter;
}
```

Для работы JWT нам потребуется настроить CORS:
```java
@Bean
public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
}
```

Далее нам нужно сделать доступным менеджер аунтентификации, т.к. по умолчанию он не виден. Поэтому сделаем его доступным принудительно:
```java
@Override @Bean
public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
}
```

Осталось закончить с самой конфигурацией:
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    // Enable CORS and disable CSRF
    http = http.cors().and().csrf().disable();

    // Set session management to stateless
    http = http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and();

    // Set unauthorized requests exception handler
    http = http.exceptionHandling()
            .authenticationEntryPoint((request, response, ex) -> response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage())).and();

    // Set permissions on endpoints
    http.authorizeRequests()
            // Our public endpoints
            .antMatchers("/api/**").permitAll()
            // Our private endpoints
            .anyRequest().authenticated();

    // Add JWT token filter
    http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
}
```

В **SecurityConfig** ещё необходимо настроить **PasswordEncoder**. О нём мы поговорим далее, а пока мы сделаем свою реализацию, которая просто возвращает пароль "как есть":
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new PasswordEncoder() {
        @Override
        public String encode(CharSequence charSequence) { return charSequence.toString(); }

        @Override
        public boolean matches(CharSequence charSequence, String s) { return s.equals(charSequence.toString()); }
    };
}

Теперь нужно создать код для входа в систему. Создадим специальный пакет "requests" для DTO пользовательского ввода. И создадим там DTO для ввода пользователя логина и пароля:
```java
@Data
public class AuthRequest {
    private String username;
    private String password;
}
```

Осталось только создать контроллер, чтобы проверить, что аутентификация работает:
```java
@RestController
@RequestMapping("/api")
public class ApiController {
```

И сделаем метод для входа в систему под УЗ пользователя:
```java
@PostMapping("login")
public ResponseEntity<UserDto> login(@RequestBody AuthRequest request) {
    try {
        Authentication authenticate = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetailsImpl userDetails = (UserDetailsImpl) authenticate.getPrincipal();
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwtService.generateAccessToken(userDetails.getUser()))
                .body(new UserDto(userDetails.getUser()));
    } catch (BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
```
**ResponseEntity** представляет целый HTTP ответ: status code, заголовки и тело ответа. Он предоставляет полный контроль над возвращаемым со стороны REST'а ответом. **RequestBody** говорит о том, что в это поле при помощи HttpMessageConverter тело запроса будет превращено в указанный объект.

Запустив приложение убедимся, что нас просто так не пускают к данным о пользователе. Выполним:
> curl https://127.0.0.1:8043/users/admin -s -S -k

Нам сказали Unauthorized. Попробуем тогда залогинится:
> curl -H "Content-Type: application/json" -d '{"username":"admin","password":"admin"}' https://127.0.0.1:8043/api/login -s -S -k -i

Скопируем токен из заголовка "Authorization" и сохраним его как переменную сеанса командной строки. Например, в bash это может выглядеть примерно так:
> TOKEN=значение

Теперь используем этот токен для запроса:
> curl -H 'Accept: application/json' -H "Authorization: Bearer ${TOKEN}" https://127.0.0.1:8043/users/admin -s -S -k

Теперь, когда мы передали полученный от сервера токен, мы смогли получить страницу.

Ура. Теперь нам осталось реализовать регистрацию нового пользователя.

------------

## [↑](#home) <a id="signup"></a> PasswordEncoder & SignUp
Ранее мы говорили, что хранение пароля в открытом виде, в том числе в БД - это плохо. Исправим это. Исправляется это просто. Нужно просто вернуть правильную реализацию **PasswordEncoder**. Заменим нашу реализацию на **BCryptPasswordEncoder**:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Далее создадим DTO для реквеста на создания пользователя по аналогии с тем, как мы ранее делали DTO для реквеста логина:
```java
@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
}
```

Далее нужно обучить наш UserService создавать пользователей:
```java
public void saveUser(String name, String password, String email) {
    User user = new User();
    user.setName(name);
    user.setEmail(email);
    user.setPassword(password);
    userRepository.save(user);
}
```

Теперь доработаем наш ApiController. Во-первых, нам понадобится добавить несколько полей:
```java
private final UserService userService;
private final PasswordEncoder passwordEncoder;
```

Теперь добавим в наш api метод регистрации пользователя:
```java
@PostMapping("register")
public UserDto register(@RequestBody CreateUserRequest request) {
    String passwd = passwordEncoder.encode(request.getPassword());
    User newUser = userService.createUser(request.getUsername(), passwd, request.getEmail());
    return new UserDto(newUser);
}
```

Т.к. мы создаём новых пользователей, мы не хотим сами указывать id, а хотим чтобы БД сама следила за этим. Поэтому нужно немного изменить entity:
```java
@Entity(name = "accounts")
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "user_id")
```

Стоит ещё одно изменение сделать на стороне БД. Т.к. теперь мы хэшируем пароли, то нужно поднять лимит длины поля пароля:
```sql
ALTER TABLE public.accounts
ALTER COLUMN "password" TYPE varchar(100);
```

Перезапустим сервер и проверим регистрацию пользователей:
> curl -H "Content-Type: application/json" -d '{"username":"admin2","password":"admin2","email":"test@example.com"}' https://127.0.0.1:8043/api/register -s -S -k

После регистрации пользователя можно попробовать под ним зайти:
> curl -H "Content-Type: application/json" -d '{"username":"admin2","password":"admin2"}' https://127.0.0.1:8043/api/login -s -S -k -i

Получив токен и сохранив его в переменную token попробуем с этим токеном выполнить запрос:
> curl -H 'Accept: application/json' -H "Authorization: Bearer ${TOKEN}" https://127.0.0.1:8043/users/admin2 -s -S -k

Если всё было сделано правильно, то оно до сих пор работает.

------------

## [↑](#home) <a id="roles"></a> GrantedAuthority
Для полноты картины осталось разобраться с ролями. Как мы помним, мы уже создали роль пользователя в виде класса Role, который реализует интерфейс GrantedAuthority. Осталось только задействовать механизм ролей.

Для начала, включим возможность управлять доступом по ролям:
```java
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
```

Далее заведём в пакете модели класс с константами для ролей:
```java
public class Roles {
    public static final String USER = "USER";
}
```

И теперь используем его для назначения роли. Например, скажем над UserController'ом, что он доступен только пользователям с правами USER:
```java
@RestController
@RequestMapping("/users")
@RolesAllowed(Roles.USER)
public class UserController {
```

Если теперь выполнить запрос как раньше, то мы получим ошибку с кодом 403 и текстом **"Forbidden"**. Чтобы это исправить необходимо добавить в БД роль и добавить её пользователю:
```sql
INSERT INTO roles(role_name) VALUES ('USER');
INSERT INTO account_roles(user_id, role_id) VALUES (2, 1);
```

Чтобы всё это заработало нужно ещё удалить префикс у ролей, который Spring добавляет по умолчанию. Задаётся это в реализации WebMvcConfigurer, т.к. наш SecurityConfig будет выполнен слишком рано:
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        // Remove the default ROLE_ prefix
        return new GrantedAuthorityDefaults("");
    }
}
```

Теперь, если повторить действия пользователю с ID 2 снова заработает контроллер, т.к. у него в БД появилась роль с ID=1, т.е. роль USER.

------------

## [↑](#home) <a id="resources"></a> Resources
Дополнительные материалы:
- [Spring Security with JWT for REST API](https://www.toptal.com/spring/spring-security-tutorial)
- [Yoh0xFF/java-spring-security-example](https://github.com/Yoh0xFF/java-spring-security-example)
- Vlad Mihalcea: [The best way to initialize LAZY entity and collection proxies with JPA and Hibernate](https://vladmihalcea.com/initialize-lazy-proxies-collections-jpa-hibernate/)
- Thorben Janssen: [5 ways to initialize lazy associations and when to use them](https://thorben-janssen.com/5-ways-to-initialize-lazy-relations-and-when-to-use-them/)
- Thorben Janssen: [Best Practices for Many-to-Many Associations](https://thorben-janssen.com/best-practices-for-many-to-many-associations-with-hibernate-and-jpa/)
- [PostgreSQL Tutorial](https://www.postgresqltutorial.com/)
- [Hibernate ORM User Guide](https://docs.jboss.org/hibernate/orm/5.5/userguide/html_single/Hibernate_User_Guide.html)