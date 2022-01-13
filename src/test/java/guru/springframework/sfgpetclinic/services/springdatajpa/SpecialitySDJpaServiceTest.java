package guru.springframework.sfgpetclinic.services.springdatajpa;

import guru.springframework.sfgpetclinic.model.Speciality;
import guru.springframework.sfgpetclinic.repositories.SpecialtyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class SpecialitySDJpaServiceTest {

    // To test exception handling we throw expections.
    // Test if it's bubbles up or is improperly handled.

    // We have a lambda strict matcher + it did not match,
    // (with argThat mockito isn't happy without a match),
    // lenient = true resolves this (default is false).
    @Mock(lenient = true)
    SpecialtyRepository specialtyRepository;

    @InjectMocks
    SpecialitySDJpaService service;

    @Test
    void testDeleteByObject() {
        //given
        Speciality speciality = new Speciality();

        //when
        service.delete(speciality);

        //then
        then(specialtyRepository).should().delete(any(Speciality.class));
    }

    @Test
    void findByIdTest() {
        //given
        Speciality speciality = new Speciality();
        given(specialtyRepository.findById(1L)).willReturn(Optional.of(speciality));

        //when
        Speciality foundSpecialty = service.findById(1L);

        //then
        assertThat(foundSpecialty).isNotNull();
        // Maybe you want to watch performance, JT hasn't had a use case for this.
        // Run time depends on your workstation / the CI server load.
        // For complex calculations it could be useful, prevents you from introducing performance issues.

        // assert runs within 100 milliseconds
        then(specialtyRepository).should(timeout(100)).findById(anyLong());
        then(specialtyRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void deleteById() {
        //given - none

        //when
        service.deleteById(1l);
        service.deleteById(1l);

        //then

        // runs within 100 milliseconds and is interacted with twice
        then(specialtyRepository).should(timeout(100).times(2)).deleteById(1L);
    }

    @Test
    void deleteByIdAtLeast() {
        //given

        //when
        service.deleteById(1l);
        service.deleteById(1l);

        //then
        // runs within 1 milliseconds!! called atLeastOnce()
        then(specialtyRepository).should(timeout(1).atLeastOnce()).deleteById(1L);
    }

    @Test
    void deleteByIdAtMost() {
        //when
        service.deleteById(1l);
        service.deleteById(1l);

        //then
        // couldn't chain timeout() with atMost() :-(
        then(specialtyRepository).should(atMost(5)).deleteById(1L);
    }

    @Test
    void deleteByIdNever() {

        //when
        service.deleteById(1l);
        service.deleteById(1l);

        //then
        // runs within 200 milliseconds works atLeastOnce()
        then(specialtyRepository).should(timeout(200).atLeastOnce()).deleteById(1L);
        then(specialtyRepository).should(never()).deleteById(5L);

    }

    @Test
    void testDelete() {
        //when
        service.delete(new Speciality());

        //then
        then(specialtyRepository).should().delete(any());
    }

    @Test
    void testDoThrow() {
        // the typical way to test exceptions
        // new RuntimeException("boom") is thrown when specialtyRepository.delete(any()) is called
        doThrow(new RuntimeException("boom")).when(specialtyRepository).delete(any());
        // verify
        assertThrows(RuntimeException.class, () -> specialtyRepository.delete(new Speciality()));

        // verify
        verify(specialtyRepository).delete(any());
    }

    // BDD approach
    @Test
    void testFindByIDThrows() {
        given(specialtyRepository.findById(1L)).willThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> service.findById(1L));

        then(specialtyRepository).should().findById(1L);
    }

    // BDD approach, handling problem if delete is called + it's not returning a value
    @Test
    void testDeleteBDD() {
        // you can't lead off the given, so a slightly different approach
        willThrow(new RuntimeException("boom")).given(specialtyRepository).delete(any());

        assertThrows(RuntimeException.class, () -> specialtyRepository.delete(new Speciality()));

        then(specialtyRepository).should().delete(any());
    }

    @Test
    void testSaveLambda() {
        //given
        // use final when the String doesn't change
        final String MATCH_ME = "MATCH_ME";
        Speciality speciality = new Speciality();
        speciality.setDescription(MATCH_ME);

        Speciality savedSpecialty = new Speciality();
        savedSpecialty.setId(1L);

        // need mock to only return savedSpecialty on match of MATCH_ME string
        // lambda argument matcher (good option to use when you are looking into the properties of an object):-
        given(specialtyRepository.save(argThat(argument -> argument.getDescription().equals(MATCH_ME)))).willReturn(savedSpecialty);

        //when
        Speciality returnedSpecialty = service.save(speciality);

        //then
        assertThat(returnedSpecialty.getId()).isEqualTo(1L);
    }

    @Test
    void testSaveLambdaNoMatch() {
        //given
        final String MATCH_ME = "MATCH_ME";
        Speciality speciality = new Speciality();
        // test description doesn't match in the mock below
        speciality.setDescription("Not a match");

        Speciality savedSpecialty = new Speciality();
        savedSpecialty.setId(1L);

        //need mock to only return on match MATCH_ME string
        given(specialtyRepository.save(argThat(argument -> argument.getDescription().equals(MATCH_ME)))).willReturn(savedSpecialty);

        //when
        Speciality returnedSpecialty = service.save(speciality);

        //then
        // this null as it didn't match
        assertNull(returnedSpecialty);
    }
}