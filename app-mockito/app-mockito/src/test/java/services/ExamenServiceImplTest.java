package services;

import models.Examen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import repositories.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// otra forma de inyectar dependencias es hacer
// @ExtendWith(MockitoExtension.class)

class ExamenServiceImplTest {
    @Mock
    ExamenRepository examenRepository;
    @Mock
    PreguntaRepository preguntaRepository;

    @InjectMocks // hace lo mismo que esta dentro del setup comentado
    ExamenServiceImpl service;  // tiene que ser con impl porque sino es una interfaz, tiene que se concreto

    @Captor
    ArgumentCaptor<Long> captor;

    @BeforeEach
    void setUp() {
        // inyeccion de dependencias
        MockitoAnnotations.openMocks(this);

        //examenRepository = mock(ExamenRepositoryOtro.class);
        //preguntaRepository = mock(PreguntaRepository.class);
        //service = new ExamenServiceImpl(examenRepository, preguntaRepository);
    }

    @Test
    void findExamenPorNombre() {
        //ExamenRepository repository = mock(ExamenRepository.class);  //new ExamenRepositoryImpl();

        /*
        esto funcionaria sin el optional, pero solo cuando no devuelve una lista vacia
        Examen examen = service.findExamenPorNombre("Matematicas");

        assertNotNull(examen);
        assertEquals(5L, examen.getId());
        assertEquals("Matemáticas", examen.getNombre());
        */

        /*

       //ANTES DE CREAR EL DOCUMENTO DATOS

        List<Examen> datos = Arrays.asList(
                new Examen(5L, "Matematicas"),
                new Examen(6L, "Lenguaje"),
                new Examen(7L, "Historia")
        );

        when(examenRepository.findAll()).thenReturn(datos); */

        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        Optional<Examen> examen = service.findExamenPorNombre("Matematicas");

        assertTrue(examen.isPresent());
        assertEquals(5L, examen.orElseThrow().getId());
        assertEquals("Matematicas", examen.get().getNombre());
    }

    @Test
    void findExamenPorNombreListaVacia() {
        List<Examen> datos = Collections.emptyList();

        when(examenRepository.findAll()).thenReturn(datos);
        Optional<Examen> examen = service.findExamenPorNombre("Matemáticas");

        assertFalse(examen.isPresent());
    }


    @Test
    void testPreguntasExamen() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);

        //when(preguntaRepository.findPreguntasPorExamenId(5L)).thenReturn(repositories.Datos.PREGUNTAS); // obtener las del id 5
        when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);
        Examen examen = service.findExamenPorNombreConPreguntas("Matematicas");

        // en el archivo de datos hya 5 preguntas, por eso no falla
        assertEquals(5, examen.getPreguntas().size());
        //assertTrue(examen.getPreguntas().contains("integrales"));

    }

    @Test
    void testPreguntasExamenVerify() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);
        Examen examen = service.findExamenPorNombreConPreguntas("Matematicas");
        assertEquals(5, examen.getPreguntas().size());
        assertTrue(examen.getPreguntas().contains("integrales"));

        // verificamos que del objeto mock examen repository se llame el findAll, sino falla
        verify(examenRepository).findAll();
        verify(preguntaRepository).findPreguntasPorExamenId(anyLong());

    }

    @Test
    void testNoExisteExamenVerify() {
        // given
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        //when(examenRepository.findAll()).thenReturn(Collections.emptyList()); // para que sea nulo el examen
        when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);

        //when
        Examen examen = service.findExamenPorNombreConPreguntas("Matematicas");

        //then
        //assertNull(examen);
        verify(examenRepository).findAll();
        verify(preguntaRepository).findPreguntasPorExamenId(5L); // falla porque el examen es nulo
    }

    @Test
    void testGuardarExamen() {
        // Given
        Examen newExamen = Datos.EXAMEN;
        newExamen.setPreguntas(Datos.PREGUNTAS);

        // cuando se invoque el guardar del repositorio de examen y pasemos
        // cualquier instancia del tipo examen, vamor a devolver un nuevo examen repositories.Datos.Examen
        when(examenRepository.guardar(any(Examen.class))).then(new Answer<Examen>(){

            Long secuencia = 8L;    // si lo cambio tengo que modificarlo en el assertEqual

            @Override
            public Examen answer(InvocationOnMock invocation) throws Throwable {
                Examen examen = invocation.getArgument(0);
                examen.setId(secuencia++);
                return examen;
            }
        });

        // When
        Examen examen = service.guardar(newExamen);

        // Then
        assertNotNull(examen.getId());
        assertEquals(8L, examen.getId());
        assertEquals("Fisica", examen.getNombre());

        verify(examenRepository).guardar(any(Examen.class));    // verificar que se guarde independientemente de lo que paso
        verify(preguntaRepository).guardarVarias(anyList());    // verificar que se invoque ese metodo
    }

    @Test
    void testManejoException() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES_ID_NULL);
        when(preguntaRepository.findPreguntasPorExamenId(isNull())).thenThrow(new IllegalArgumentException());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.findExamenPorNombreConPreguntas("Historia"); //falla con matematicas
        });
        assertEquals(IllegalArgumentException.class, exception.getClass());

        verify(examenRepository).findAll();
        verify(preguntaRepository).findPreguntasPorExamenId(isNull());
    }

    // se usa para asegurarnos de que ciertos argumentos se pasen
    @Test
    void testArgumentMatchers() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES); // falla con repositories.Datos.EXAMENES_ID_NULL
        when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);

        service.findExamenPorNombreConPreguntas("Matematicas");

        verify(examenRepository).findAll();
        verify(preguntaRepository).findPreguntasPorExamenId(argThat(arg -> arg != null && arg.equals(5L)));
        //verify(preguntaRepository).findPreguntasPorExamenId(argThat(arg -> arg != null && arg > 5l)); // falla
        //verify(preguntaRepository).findPreguntasPorExamenId(eq(5L));
    }

    // valida y muestra el mensaje personalizado
    @Test
    void testArgumentMatchers2() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES); // falla con repositories.Datos.EXAMENES_ID_NEGATIVOS
        when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);

        service.findExamenPorNombreConPreguntas("Matematicas");

        verify(examenRepository).findAll();
        verify(preguntaRepository).findPreguntasPorExamenId(argThat(new MiArgsMatchers()));
    }

    // solo valida pero no muestra el mensaje personalizado
    @Test
    void testArgumentMatchers3() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES); // falla con repositories.Datos.EXAMENES_ID_NEGATIVOS
        when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);

        service.findExamenPorNombreConPreguntas("Matematicas");

        verify(examenRepository).findAll();
        verify(preguntaRepository).findPreguntasPorExamenId(argThat((argument) -> argument != null && argument > 0));
    }

    public static class MiArgsMatchers implements ArgumentMatcher<Long> {

        private Long argument;

        @Override
        public boolean matches(Long argument) {
            this.argument = argument;
            return argument != null && argument > 0;
        }

        @Override
        public String toString() {
            return "es para un mensaje personalizado de error " +
                    "que imprime mockito en caso de que falle el test "
                    + argument + " debe ser un entero positivo";
        }
    }

    @Test
    void testArgumentCaptor() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        //when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(repositories.Datos.PREGUNTAS);
        service.findExamenPorNombreConPreguntas("Matematicas");

        //ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);    // se encuentra arriba del archivo en las anotaciones
        verify(preguntaRepository).findPreguntasPorExamenId(captor.capture());

        assertEquals(5L, captor.getValue());
    }

    @Test
    void testDoThrow() {
        Examen examen = Datos.EXAMEN;
        examen.setPreguntas(Datos.PREGUNTAS);
        doThrow(IllegalArgumentException.class).when(preguntaRepository).guardarVarias(anyList());

        assertThrows(IllegalArgumentException.class, () -> {
            service.guardar(examen);
        });
    }

    @Test
    void testDoAnswer() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        //when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(repositories.Datos.PREGUNTAS);
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            // si el id es 5 devuelvo las preguntas, sino null
            return id == 5L? Datos.PREGUNTAS: Collections.emptyList();
        }).when(preguntaRepository).findPreguntasPorExamenId(anyLong());

        Examen examen = service.findExamenPorNombreConPreguntas("Matematicas");
        assertEquals(5, examen.getPreguntas().size());
        assertTrue(examen.getPreguntas().contains("geometria"));
        assertEquals(5L, examen.getId());
        assertEquals("Matematicas", examen.getNombre());

        verify(preguntaRepository).findPreguntasPorExamenId(anyLong());
    }

    @Test
    void testDoAnswerGuardarExamen() {
        // Given
        Examen newExamen = Datos.EXAMEN;
        newExamen.setPreguntas(Datos.PREGUNTAS);

        doAnswer(new Answer<Examen>(){

            Long secuencia = 8L;

            @Override
            public Examen answer(InvocationOnMock invocation) throws Throwable {
                Examen examen = invocation.getArgument(0);
                examen.setId(secuencia++);
                return examen;
            }
        }).when(examenRepository).guardar(any(Examen.class));

        // When
        Examen examen = service.guardar(newExamen);

        // Then
        assertNotNull(examen.getId());
        assertEquals(8L, examen.getId());
        assertEquals("Fisica", examen.getNombre());

        verify(examenRepository).guardar(any(Examen.class));
        verify(preguntaRepository).guardarVarias(anyList());
    }

    /*// para cuando no puedo simular un meotodo
    @Test
    void testDoCallRealMethod() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        //when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(repositories.Datos.PREGUNTAS);
        doCallRealMethod().when(preguntaRepository).findPreguntasPorExamenId(anyLong());
        Examen examen = service.findExamenPorNombreConPreguntas("Matemáticas");
        assertEquals(5L, examen.getId());
        assertEquals("Matematicas", examen.getNombre());

    }*/

    // llama metodos reales, solo simula con when
    @Test
    void testSpy() {
        ExamenRepository examenRepository = spy(ExamenRepositoryImpl.class);
        PreguntaRepository preguntaRepository = spy(PreguntaRepositoryImpl.class);
        ExamenService examenService = new ExamenServiceImpl(examenRepository, preguntaRepository);

        List<String> preguntas = Arrays.asList("aritmetica");
        //when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(preguntas);
        doReturn(preguntas).when(preguntaRepository).findPreguntasPorExamenId(anyLong());

        Examen examen = examenService.findExamenPorNombreConPreguntas("Matematicas");
        assertEquals(5, examen.getId());
        assertEquals("Matematicas", examen.getNombre());
        assertEquals(1, examen.getPreguntas().size());
        assertTrue(examen.getPreguntas().contains("aritmetica"));

        verify(examenRepository).findAll();
        verify(preguntaRepository).findPreguntasPorExamenId(anyLong());
    }

    @Test
    void testOrdenDeInvocaciones() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);

        service.findExamenPorNombreConPreguntas("Matematicas");
        service.findExamenPorNombreConPreguntas("Lenguaje");

        InOrder inOrder = inOrder(preguntaRepository);
        inOrder.verify(preguntaRepository).findPreguntasPorExamenId(5L);
        inOrder.verify(preguntaRepository).findPreguntasPorExamenId(6L);

    }

    @Test
    void testOrdenDeInvocaciones2() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);

        service.findExamenPorNombreConPreguntas("Matematicas");
        service.findExamenPorNombreConPreguntas("Lenguaje");

        InOrder inOrder = inOrder(examenRepository, preguntaRepository);
        inOrder.verify(examenRepository).findAll();
        inOrder.verify(preguntaRepository).findPreguntasPorExamenId(5L);

        inOrder.verify(examenRepository).findAll();
        inOrder.verify(preguntaRepository).findPreguntasPorExamenId(6L);

    }

    @Test
    void testNumeroDeInvocaciones() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        service.findExamenPorNombreConPreguntas("Matematicas");

        verify(preguntaRepository).findPreguntasPorExamenId(5L);
        verify(preguntaRepository, times(1)).findPreguntasPorExamenId(5L);
        verify(preguntaRepository, atLeast(1)).findPreguntasPorExamenId(5L);
        verify(preguntaRepository, atLeastOnce()).findPreguntasPorExamenId(5L);
        verify(preguntaRepository, atMost(1)).findPreguntasPorExamenId(5L);
        verify(preguntaRepository, atMostOnce()).findPreguntasPorExamenId(5L);
    }

    @Test
    void testNumeroDeInvocaciones2() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        service.findExamenPorNombreConPreguntas("Matematicas");

        //verify(preguntaRepository).findPreguntasPorExamenId(5L); falla
        verify(preguntaRepository, times(1)).findPreguntasPorExamenId(5L); // si coloco dos veces falla
        verify(preguntaRepository, atLeast(1)).findPreguntasPorExamenId(5L);
        verify(preguntaRepository, atLeastOnce()).findPreguntasPorExamenId(5L);
        verify(preguntaRepository, atMost(20)).findPreguntasPorExamenId(5L);
        //verify(preguntaRepository, atMostOnce()).findPreguntasPorExamenId(5L); falla
    }

    @Test
    void testNumeroInvocaciones3() {
        when(examenRepository.findAll()).thenReturn(Collections.emptyList());
        service.findExamenPorNombreConPreguntas("Matematicas");

        verify(preguntaRepository, never()).findPreguntasPorExamenId(5L);
        verifyNoInteractions(preguntaRepository);

        verify(examenRepository).findAll();
        verify(examenRepository, times(1)).findAll();
        verify(examenRepository, atLeast(1)).findAll();
        verify(examenRepository, atLeastOnce()).findAll();
        verify(examenRepository, atMost(10)).findAll();
        verify(examenRepository, atMostOnce()).findAll();
    }
}