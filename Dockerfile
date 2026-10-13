# ---------- Etapa 1: compilación con Maven (JDK 17) ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Los POM primero: la capa de dependencias se reutiliza mientras no cambien
COPY pom.xml .
COPY rm-domain/pom.xml   rm-domain/
COPY rm-business/pom.xml rm-business/
COPY rm-data/pom.xml     rm-data/
COPY rm-web/pom.xml      rm-web/
COPY rm-ear/pom.xml      rm-ear/
COPY rm-client/pom.xml   rm-client/
RUN mvn -B -q -pl rm-domain,rm-business,rm-data,rm-web,rm-ear dependency:go-offline || true

COPY rm-domain   rm-domain/
COPY rm-business rm-business/
COPY rm-data     rm-data/
COPY rm-web      rm-web/
COPY rm-ear      rm-ear/
# El cliente remoto no se despliega: se ejecuta fuera del contenedor
RUN mvn -B package -pl rm-domain,rm-business,rm-data,rm-web,rm-ear

# ---------- Etapa 2: WildFly 37 (Jakarta EE 10) ----------
FROM quay.io/wildfly/wildfly:37.0.1.Final-jdk17

COPY docker/wildfly/configurar.cli /tmp/configurar.cli

# Datasource H2 y usuario de aplicación para las invocaciones remotas de EJB
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/configurar.cli \
 && /opt/jboss/wildfly/bin/add-user.sh -a -u 'rmclient' -p 'Rm.client1!' -g 'user' --silent \
 && rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history \
           /opt/jboss/wildfly/standalone/data /opt/jboss/wildfly/standalone/log

COPY --from=build /build/rm-ear/target/resource-management.ear \
                  /opt/jboss/wildfly/standalone/deployments/resource-management.ear

EXPOSE 8080 9990
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
