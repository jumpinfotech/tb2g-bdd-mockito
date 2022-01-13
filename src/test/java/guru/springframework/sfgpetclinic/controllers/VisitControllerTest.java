package guru.springframework.sfgpetclinic.controllers;

import guru.springframework.sfgpetclinic.model.Pet;
import guru.springframework.sfgpetclinic.model.Visit;
import guru.springframework.sfgpetclinic.services.VisitService;
import guru.springframework.sfgpetclinic.services.map.PetMapService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VisitControllerTest {

    @Mock
    VisitService visitService;

    // JT doesn't use Spies much,
    // he's used them in edge cases + sometimes it's easier to allow a concrete class to do it's thing.
    // It depends on your use case.

    // Spies act as a wrapper around the real implementation.
    // interface PetService extends CrudService> the concrete implementation is PetMapService.

    // The original test had a @Mock of the PetService interface, nothing was done with a concrete class.
    // @Mock
    // PetService petService;
    // A spy allows you to access the underlying object but allows you to verify interactions,
    // allowing you to treat it like a Mock.

    // Here the @Spy is on the PetMapService concrete implementation:-
    @Spy //@Mock
     PetMapService petService;

    @InjectMocks
    VisitController visitController;

    @Test
    void loadPetWithVisit() {
        //given
        Map<String, Object> model = new HashMap<>();
        Pet pet = new Pet(12L);
        Pet pet3 = new Pet(3L);

        // persist
        petService.save(pet);
        petService.save(pet3);
        // call real method
        given(petService.findById(anyLong())).willCallRealMethod(); //.willReturn(pet);

        //when
        // It returns the item,
        // in the service we're looking-up using the "long" key value, and it gets the map value.
        Visit visit = visitController.loadPetWithVisit(12L, model);


        //then
        assertThat(visit).isNotNull();
        assertThat(visit.getPet()).isNotNull();
        // assert saved object's ID
        assertThat(visit.getPet().getId()).isEqualTo(12L);
        // petService.findById(anyLong()) is called once
        verify(petService, times(1)).findById(anyLong());
    }

    // We can do stubbing. we can tell a Spy to return back a value, just like a Mock.
    @Test
    void loadPetWithVisitWithStubbing() {
        //given
        Map<String, Object> model = new HashMap<>();
        Pet pet = new Pet(12L);
        Pet pet3 = new Pet(3L);

        petService.save(pet);
        petService.save(pet3);

        // Spy overrides real method and returns pet3
        given(petService.findById(anyLong())).willReturn(pet3);

        //when
        // we query using 12L but we will get a pet3 back
        Visit visit = visitController.loadPetWithVisit(12L, model);

        // The mock action is returning + not the real method, because we are passing 12 to the petService.
        // JT - the Spy is somehow intercepting that + the real method isn't hit.
        // The configured value on the Spy is returned + not the value the underlying implementation would have
        // returned.

        //then
        assertThat(visit).isNotNull();
        assertThat(visit.getPet()).isNotNull();
        // verify the id 3L is returned by the @Spy
        assertThat(visit.getPet().getId()).isEqualTo(3L);
        verify(petService, times(1)).findById(anyLong());
    }

}