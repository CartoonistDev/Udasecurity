package security;

import image.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import security.application.StatusListener;
import security.data.*;
import security.service.SecurityService;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    SecurityService securityService;

    private Sensor sensor;
    private final String random = UUID.randomUUID().toString();

    @Mock
    SecurityRepository securityRepository;

    @Mock
    ImageService imageService;

    private Set<Sensor> getAllSensors(int numOfSensor, boolean status) {
        Set<Sensor> sensors = new HashSet<>();
        for (int i = 0; i < numOfSensor; i++) {
            sensors.add(new Sensor(random, SensorType.DOOR));
        }
        sensors.forEach(sensor -> sensor.setActive(status));

        return sensors;
    }

    @BeforeEach
    void init(){
        securityService = new SecurityService(securityRepository, imageService);
        sensor = getNewSensor();
    }
    private Sensor getNewSensor() {
        return new Sensor(random, SensorType.DOOR);
    }


    //1. If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    @Test
    void ifAlarmIsArmedAndASensorBecomesActivated_putTheSystemIntoPendingAlarmStatus(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    //2. If alarm is armed and a sensor becomes activated
    // and the system is already pending alarm, set the alarm status to alarm.
    @Test
    void ifAlarmIsArmedAndASensorBecomesActivatedAndTheSystemIsAlreadyPending_setAlarmStatusToAlarm(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    //3. If pending alarm and all sensors are inactive, return to no alarm state.
    @Test
    void ifPendingAlarmAndAllSensorsAreInactive_returnNoAlarmState(){

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //4. If alarm is active, change in sensor state should not affect the alarm state.
    @Test
    void ifAlarmIsActiveChangeInSensorStateShould_notAffectTheAlarmState(){
        //When two sensors are on and one goes off, but alarm is still on
        // the inactive state of the sensor shouldn't change the state of the active alarm
        sensor.setActive(true);
        securityRepository.setAlarmStatus(AlarmStatus.ALARM);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor,false);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);}


    //5. If a sensor is activated while already active and
    // the system is in pending state, change it to alarm state.

    @Test
    void ifASensorIsActivatedWhileAlreadyActiveAndTheSystemIsInPendingState_changeToAlarmState(){


        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, true);


        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    //6. If a sensor is deactivated while already inactive, make no changes to the alarm state.
    @ParameterizedTest
    @EnumSource(value = AlarmStatus.class, names = {"NO_ALARM", "PENDING_ALARM", "ALARM"})
    void ifASensorIsDeactivatedWhileAlreadyInactive_makeNoChangesToTheAlarmState(){
        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    //7. If the image service identifies an image containing a cat
    // while the system is armed-home, put the system into alarm status.
    @Test
    void ifImageServiceIdentifiesCatWhileSystemIsArmedHome_putSystemIntoAlarmStatus(){

        //Creating a BufferedImage catImage for the test with
        //width, height and type
        BufferedImage catImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(true);

        securityService.processImage(catImage);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    //8. If the image service identifies an image that does not contain a cat,
    // change the status to no alarm as long as the sensors are not active.
    @Test
    void ifTheImageServiceIdentifiesImageThatDoesNotContainCat_changeTheStatusToNoAlarmAsLongAsTheSensorsAreNotActive(){
        when(imageService.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(false);
        securityService.processImage(mock(BufferedImage.class));

        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //9. If the system is disarmed, set the status to no alarm.
    @Test
    void ifTheSystemIsDisarmed_setTheStatusToNoAlarm(){
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //10. If the system is armed, reset all sensors to inactive.
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void ifTheSystemIsArmed_resetAllAlarmStatusToInactive(ArmingStatus status){
        Set<Sensor> sensors = getAllSensors(3, true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getSensors()).thenReturn(sensors);
        securityService.setArmingStatus(status);
        securityService.getSensors().forEach(sensor -> {
            assert Boolean.FALSE.equals(sensor.getActive());
        });
    }

    //11. If the system is armed-home while the camera shows a cat,
    // set the alarm status to alarm.
    @Test
    void ifTheSystemIsArmedHomeWhileTheCameraShowsACat_setTheAlarmStatusToAlarm(){
        BufferedImage catImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(any(),anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);

        securityService.processImage(catImage);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);

    }

    @Test
    void ifTheAlarmStatusPendingAndSensorIsNotActive_DeactivateSensor(){

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor);
        securityService.changeSensorActivationStatus(sensor, false);

        securityService.getSensors().forEach(sensor -> assertFalse(sensor.getActive()));
    }

    @Test
    void ifAlarmStateAndSystemIsDisarmed_changeStatusToPending() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        securityService.changeSensorActivationStatus(sensor);

        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }



}
