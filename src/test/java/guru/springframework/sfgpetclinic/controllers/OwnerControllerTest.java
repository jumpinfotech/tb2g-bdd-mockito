package guru.springframework.sfgpetclinic.controllers;

import guru.springframework.sfgpetclinic.fauxspring.BindingResult;
import guru.springframework.sfgpetclinic.fauxspring.Model;
import guru.springframework.sfgpetclinic.model.Owner;
import guru.springframework.sfgpetclinic.services.OwnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    // private string constants help improve the quality of tests
    private static final String OWNERS_CREATE_OR_UPDATE_OWNER_FORM = "owners/createOrUpdateOwnerForm";
    private static final String REDIRECT_OWNERS_5 = "redirect:/owners/5";

    @Mock
    OwnerService ownerService;

    // need a Mock model, e.g. to call model.addAttribute("selections", results);
    @Mock
    Model model;

    @InjectMocks
    OwnerController controller;

    // BindingResult is an interface, we are mimicking spring framework
    @Mock
    BindingResult bindingResult;

    // annotation based ArgumentCaptor, saves duplication when using ArgumentCaptor<String> in many tests.
    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;

    @BeforeEach
    void setUp() {
        // Class level @Captor is better, but this can be done:-
        // final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        // get value passed into the Mock:-
        // given(ownerService.findAllByLastNameLike(captor.capture())) .....

        // Argument capture gets a list of values, if it's invoked more than once we'll get a list of multiple values.
        // Here it is invoked only once so getValue is perfect.

        // assertThat("%Buck%").isEqualToIgnoringCase(captor.getValue());

        // stringArgumentCaptor capture the value
        given(ownerService.findAllByLastNameLike(stringArgumentCaptor.capture()))
                .willAnswer(invocation -> {
            List<Owner> owners = new ArrayList<>();

            String name = invocation.getArgument(0); // get value

            // returned value depends upon the value captured
            // OwnerController.processFindForm has if elses to test:-
            if (name.equals("%Buck%")) { // switch would be cleaner
               // for a view with 1 owner shown
                owners.add(new Owner(1l, "Joe", "Buck"));
                return owners;
            } else if (name.equals("%DontFindMe%")) {
                return owners; // DontFindMe wasn't found, an empty list is returned
            } else if (name.equals("%FindMe%")) {
               // probably a table view showing owners
                owners.add(new Owner(1l, "Joe", "Buck"));
                owners.add(new Owner(2l, "Joe2", "Buck2"));
                return owners;
            }
            // fail test if an unexpected value is passed in
            throw new RuntimeException("Invalid Argument");
        });
    }

    @Test
    void processFindFormWildcardFound() {
        //given
        Owner owner = new Owner(1l, "Joe", "FindMe");
        // Verify ownerService is called before model.
        // Pass in mocks, the order here doesn't seem to matter, verify order below matters:-
        InOrder inOrder = inOrder(ownerService, model);

        //when
        String viewName = controller.processFindForm(owner, bindingResult, model); // take in model Mock

        //then
        assertThat("%FindMe%").isEqualToIgnoringCase(stringArgumentCaptor.getValue());
        // verify owner found
        assertThat("owners/ownersList").isEqualToIgnoringCase(viewName);

        // inorder asserts
        // verify ownerService is called before model (order matters here, and not above):-
        inOrder.verify(ownerService).findAllByLastNameLike(anyString());
        // anyList() - make sure we get a list, Mock model is interacted with once.
        inOrder.verify(model, times(1)).addAttribute(anyString(), anyList());
        // Mock model has no more interactions, Peter thinks pointless??? We checked for 1 interaction above.
        verifyNoMoreInteractions(model);
    }

    @Test
    void processFindFormWildcardStringAnnotation() {
        //given
        Owner owner = new Owner(1l, "Joe", "Buck");

        //when
        String viewName = controller.processFindForm(owner, bindingResult, null);

        //then
        // stringArgumentCaptor get the value
        assertThat("%Buck%").isEqualToIgnoringCase(stringArgumentCaptor.getValue());
        assertThat("redirect:/owners/1").isEqualToIgnoringCase(viewName);
        // Mock model has no interactions
        verifyZeroInteractions(model);
    }


    @Test
    void processFindFormWildcardNotFound() {
        //given
        Owner owner = new Owner(1l, "Joe", "DontFindMe"); // won't be found

        //when
        String viewName = controller.processFindForm(owner, bindingResult, null);

        verifyNoMoreInteractions(ownerService);

        //then
        assertThat("%DontFindMe%").isEqualToIgnoringCase(stringArgumentCaptor.getValue());
        // DontFindMe wasn't found, therefore viewName returned = "owners/findOwners"
        assertThat("owners/findOwners").isEqualToIgnoringCase(viewName);
        verifyZeroInteractions(model);
    }

    @Test
    void processCreationFormHasErrors() {
        //given
        Owner owner = new Owner(1l, "Jim", "Bob");
        // hasErrors() = true
        given(bindingResult.hasErrors()).willReturn(true);

        //when
        String viewName = controller.processCreationForm(owner, bindingResult);

        //then
        // return to create or update form
        assertThat(viewName).isEqualToIgnoringCase(OWNERS_CREATE_OR_UPDATE_OWNER_FORM);
    }

    @Test
    void processCreationFormNoErrors() {
        //given
        // Could argue that the Owner object being passed in should be different to what the mock returns.
        // However, there's no transformative logic when the service persists the Owner object.
        Owner owner = new Owner(5l, "Jim", "Bob");
        // process without errors
        given(bindingResult.hasErrors()).willReturn(false);
        given(ownerService.save(any())).willReturn(owner);

        //when
        String viewName = controller.processCreationForm(owner, bindingResult);

        //then
        // redirect to the view form = redirect:/owners/5,
        // Owner id = 5
        assertThat(viewName).isEqualToIgnoringCase(REDIRECT_OWNERS_5);
    }
}