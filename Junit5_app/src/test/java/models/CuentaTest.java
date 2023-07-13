package models;

// el plugin de maven permite ejecutar pruebas unitarias por consola

import exceptions.DineroInsuficienteException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumingThat;

// Hace que podamos quitar el static de beforeAll y afterAll
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)

class CuentaTest {
    Cuenta cuenta;

    // Proporciona información sobre el contexto de ejecución de una prueba
    private TestInfo testInfo;
    private TestReporter testReporter;

    @BeforeAll
    static void beforeAll() {
        System.out.println("inicializando el test");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("finalizando el test");
    }

    static List<String> montoList() {
        return Arrays.asList("100", "200", "300", "500", "700", "1000.12345");
    }

    @BeforeEach
    void initMetodoTest(TestInfo testInfo, TestReporter testReporter) {
        this.cuenta = new Cuenta("Gaby", new BigDecimal("1000.12345"));

        // Para usar los valores en cada test
        this.testInfo = testInfo;
        this.testReporter = testReporter;
        System.out.println("iniciando el metodo.");
        testReporter.publishEntry(" ejecutando: " + testInfo.getDisplayName() + " " + testInfo.getTestMethod().orElse(null).getName()
                + " con las etiquetas " + testInfo.getTags());
    }

    @AfterEach
    void tearDown() {
        System.out.println("finalizando el metodo de prueba");
    }

    // ---- FIN DE TESTS CONDICIONALES ----

    @Test
    @Tag("cuenta")
    @Tag("banco")
    // @Disabled // hace que se salte la prueba de este test
    @DisplayName("probando relaciones entre las cuentas y el banco con assertAll")
    void testRelacionBancoCuentas() {
        // fail(); // forza que falle el test
        Cuenta cuenta1 = new Cuenta("Soto", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("Gaby", new BigDecimal("1500.8989"));

        Banco banco = new Banco();

        banco.addCuenta(cuenta1);
        banco.addCuenta(cuenta2);

        banco.setNombre("Banco del Estado");
        banco.transferir(cuenta2, cuenta1, new BigDecimal(500));

        assertAll(
                () -> {
                    assertEquals("1000.8989", cuenta2.getSaldo().toPlainString(),
                            () -> "El valor de saldo de la cuenta2 no es el esperado");
                },
                () -> {
                    assertEquals("3000", cuenta1.getSaldo().toPlainString(),
                            () -> "El valor de saldo de la cuenta1 no es el esperado");
                },
                () -> {
                    assertEquals(2, banco.getCuentas().size(),
                            () -> "El banco no tiene las cuentas esperadas");
                },
                () -> {
                    assertEquals("Banco del Estado", cuenta1.getBanco().getNombre());
                },

                // encuentra el usuario gaby en la lista de cuentas del banco
                () -> {
                    assertEquals("Gaby", banco.getCuentas().stream()
                            .filter(c -> c.getPersona().equals("Gaby"))
                            .findFirst().get().getPersona()
                    );
                },

                // ve si hay algun match con gaby en la lista de cuentas del banco
                () -> {
                    assertTrue(banco.getCuentas().stream()
                            .anyMatch(c -> c.getPersona().equals("Gaby"))
                    );
                }
        );
    }

    @Test
    @Tag("cuenta")
    @Tag("error")
    @DisplayName("probando excepeciones de monto en cuenta")
    void testDineroInsuficienteExceptionCuenta() {
        Cuenta cuenta = new Cuenta("Gaby", new BigDecimal("1000.12345"));

        // simulamos el error, en la cuenta no hay 1500
        Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
            cuenta.debito(new BigDecimal(1500));
        });
        String actual = exception.getMessage();
        String esperado = "Dinero Insuficiente";

        //si cambio el mensaje no pasa la prueba
        // String esperado = "Dinero Insuficiente en la cuenta"; // falla
        assertEquals(esperado, actual);
    }

    /*
    en configuracion para correr CuentaTest debo tener
    -ea -DENV=prod
    enviroment variables: ENVIROMENT=prod
    */
    @Test
    @DisplayName("test Saldo Cuenta Dev")
    void testSaldoCuentaDev() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumeTrue(esDev);
        assertNotNull(cuenta.getSaldo());
        assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("test Saldo Cuenta Dev 2")
    void testSaldoCuentaDev2() {

        // no se ejecuta porque tenemos prod y no dev
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumingThat(esDev, () -> {
            assertNotNull(cuenta.getSaldo());
            assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
        });

        // se ejecuta porque no necesitamos que sea dev
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @DisplayName("Probando Debito Cuenta Repetir!")
    // permite repetir/ejecutar un test multiples veces
    @RepeatedTest(value = 5, name = "{displayName} - Repetición numero {currentRepetition} de {totalRepetitions}")
    void testDebitoCuentaRepetir(RepetitionInfo info) {
        // el parametro que paso me permite hacer algo en una repeticion especifica
        if (info.getCurrentRepetition() == 3) {
            System.out.println("estamos en la repeticion " + info.getCurrentRepetition());
        }
        cuenta.debito(new BigDecimal(100));
        assertNotNull(cuenta.getSaldo());
        assertEquals(900, cuenta.getSaldo().intValue());
        assertEquals("900.12345", cuenta.getSaldo().toPlainString());
    }

    @Tag("param")
    @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
    @MethodSource("montoList")
    void testDebitoCuentaMethodSource(String monto) {
        cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo());
        // falla con el ultimo numero de la lista
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @Tag("cuenta")
    @Nested
    @DisplayName("probando atributos de la cuenta corriente")
    class CuentaTestNombreSaldo {
        @Test
        @DisplayName("el nombre!")
        void testNombreCuenta() {
            // Se usa testInfo para obtener las etiquetas asociadas a la prueba actual utilizando el método getTags()
            testReporter.publishEntry(testInfo.getTags().toString());
            if (testInfo.getTags().contains("cuenta")) {
                // Se utiliza testReporter para publicar información o mensajes durante la ejecución de las pruebas
                testReporter.publishEntry("hacer algo con la etiqueta cuenta");
            }
            //cuenta.setPersona("Gaby");
            String esperado = "Gaby";
            String real = cuenta.getPersona();
            assertNotNull(real, () -> "La cuenta no puede ser nula"); // debe existir persona, no puede ser nulo
            assertEquals(esperado, real, () -> "el nombre de la cuenta no es el que se esperaba: se esperaba " + esperado
                    + " sin embargo fue " + real); // si el real es upperCase, falla
            assertTrue(real.equals("Gaby"), () -> "Nombre cuenta esperada debe ser igual a la real"); // si el real es upperCase, falla
        }

        @Test
        @DisplayName("probando el saldo de la cuenta, que no sea null, mayor que cero, valor esperado")
        void testSaldoCuenta() {
            // Cuenta cuenta = new Cuenta("Gaby", new BigDecimal("-1000.12345")); // simula falla por saldo menor a cero
            assertNotNull(cuenta.getSaldo()); // verifica que el saldo no sea nulo
            assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0); // saldo < 0 da error
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0); // saldo < 0 da error
        }

        @Test
        @DisplayName("testeando referencias que sean iguales con el metodo equals")
        void testReferenciaCuenta() {
            Cuenta cuenta1 = new Cuenta("Gaby", new BigDecimal("8900.9997"));
            Cuenta cuenta2 = new Cuenta("Gaby", new BigDecimal("8900.9997"));

        /* si no se encuentra comentado el override de la funcion equal, falla
        sino, funciona ya que son dos referencias distintas */
            // assertNotEquals(cuenta1, cuenta2);

        /* si se encuentra comentado el override de la funcion equal ocasiona falla ya que son dos instancias distintas
        sino, no falla porque en realidad comparamos el contenido y no las referencias */
            assertEquals(cuenta1, cuenta2);
        }

    }

    @Nested
    class CuentaOperacionesTest {
        @Tag("cuenta")
        @Test
        @DisplayName("probando debitar de una cuenta")
        void testDebitoCuenta() {
            cuenta.debito(new BigDecimal(100)); // se le resta 100 a la cuenta corriente

            // cuenta.debito(new BigDecimal(2000)); // tira exception por dinero insuficiente

            assertNotNull(cuenta.getSaldo());
            assertEquals(900, cuenta.getSaldo().intValue()); // es correcto

        /* verifico si el saldo es igual en strings
        como habia restado 100, el valor actual es 900 y no 1000 */
            assertEquals("900.12345", cuenta.getSaldo().toPlainString()); // es correcto
            //assertEquals("1000.12345", cuenta.getSaldo().toPlainString()); // genera falla
        }

        @Tag("cuenta")
        @Test
        void testCreditoCuenta() {
            cuenta.credito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(1100, cuenta.getSaldo().intValue());
            assertEquals("1100.12345", cuenta.getSaldo().toPlainString());
        }

        @Tag("cuenta")
        @Tag("banco")
        @Test
        @DisplayName("probando transferir dinero entre cuentas")
        void testTransferirDineroCuenta() {
            Cuenta cuenta1 = new Cuenta("Soto", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("Gaby", new BigDecimal("1500.8989"));

            Banco banco = new Banco();
            banco.setNombre("Banco del Estado");
            banco.transferir(cuenta2, cuenta1, new BigDecimal(500));

            assertEquals("1000.8989", cuenta2.getSaldo().toPlainString());
            assertEquals("3000", cuenta1.getSaldo().toPlainString());
        }
    }

    // ---- INICIO DE TESTS CONDICIONALES ----
    /*
    en configuracion para correr CuentaTest debo tener
    -ea -DENV=dev
    enviroment variables: ENVIROMENT=dev
    */
    @Nested
    class SistemaOperativoTest {
        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testSoloWindows() {
        }

        @Test
        @EnabledOnOs({OS.LINUX, OS.MAC})
        void testSoloLinuxMac() {
        }

        @Test
        @DisabledOnOs(OS.WINDOWS)
        void testNoWindows() {
        }
    }

    @Nested
    class JavaVersionTest {
        @Test
        @EnabledOnJre(JRE.JAVA_8)
        void soloJdk8() {
        }

        @Test
        @EnabledOnJre(JRE.JAVA_15)
        void soloJDK15() {
        }

        @Test
        @DisabledOnJre(JRE.JAVA_15)
        void testNoJDK15() {
        }
    }

    @Nested
    class SistemPropertiesTest {
        @Test
        void imprimirSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((k, v) -> System.out.println(k + ":" + v));
        }

        @Test
        @EnabledIfSystemProperty(named = "java.version", matches = ".*15.*")
        void testJavaVersion() {
        }

        @Test
        @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testSolo64() {
        }

        @Test
        @EnabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testNO64() {
        }

        @Test
        @EnabledIfSystemProperty(named = "user.name", matches = "aguzm")
        void testUsername() {
        }

        @Test
        @EnabledIfSystemProperty(named = "ENV", matches = "dev")
        void testDev() {
        }
    }

    @Nested
    class VariableAmbienteTest {
        @Test
        void imprimirVariablesAmbiente() {
            Map<String, String> getenv = System.getenv();
            getenv.forEach((k, v) -> System.out.println(k + " = " + v));
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "JAVA_HOME", matches = ".*jdk-15.0.1.*")
        void testJavaHome() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "NUMBER_OF_PROCESSORS", matches = "8")
        void testProcesadores() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "dev")
        void testEnv() {
        }

        @Test
        @DisabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "prod")
        void testEnvProdDisabled() {
        }

    }

    @Tag("param")
    @Nested
            // las pruebas parametrizadas me permiten repetirla pero pasando un dato de entrada distinto
    class PruebasParametrizadasTest {

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @ValueSource(strings = {"100", "200", "300", "500", "700"})
            // falla si agrego "1000.12345"
        void testDebitoCuentaValueSource(String monto) {
            //monto se reemplaza por los numeros de ValueSource
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            //veo que el saldo sea valido
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvSource({"1,100", "2,200", "3,300", "4,500", "5,700"})
            // falla si agrego "6,1000.12345"
        void testDebitoCuentaCsvSource(String index, String monto) {
            System.out.println(index + " -> " + monto);
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            // si es cero falla
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvSource({"200,100,John,Andres", "250,200,Pepe,Pepe", "300,300,maria,Maria", "510,500,Pepa,Pepa", "750,700,Lucas,Luca", "1000.12345,1000.12345,Cata,Cata"})
        void testDebitoCuentaCsvSource2(String saldo, String monto, String esperado, String actual) {
            System.out.println(saldo + " -> " + monto);
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto));
            cuenta.setPersona(actual);

            assertNotNull(cuenta.getSaldo());
            assertNotNull(cuenta.getPersona());
            assertEquals(esperado, actual);

            // solamente pasan el 2 y el 4 por los nombres
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvFileSource(resources = "/data.csv")
        void testDebitoCuentaCsvFileSource(String monto) {
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());

            // falla con el ultimo del archivo
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvFileSource(resources = "/data2.csv")
        void testDebitoCuentaCsvFileSource2(String saldo, String monto, String esperado, String actual) {
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto));
            cuenta.setPersona(actual);

            assertNotNull(cuenta.getSaldo());
            assertNotNull(cuenta.getPersona());
            assertEquals(esperado, actual);

            // solo pasan el 2 y el 4 por los datos del archivo, nombre y montos
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

    }

    @Nested
    @Tag("timeout")
            // se usa por si demoran mucho las pruebas
    class EjemploTimeoutTest {
        @Test
        @Timeout(1)
            // cantidad de segundos esperados para fallar, falla con 5
        void pruebaTimeout() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        @Test
        @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
            // 1 segundo de espera
        void pruebaTimeout2() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(900); // falla por arriba de mil
        }

        @Test
        void testTimeoutAssertions() {
            assertTimeout(Duration.ofSeconds(5), () -> {
                // simula una carga pesada
                TimeUnit.MILLISECONDS.sleep(4000); // falla con 5000
            });
        }
    }
}
