package com.healthify.guardian.platform.mobilitygeofencing.application.internal.queryservices;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.entities.CallLogEntry;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.repositories.CallLogRepository;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.WristbandCapabilityProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommunicationQueryService {

    private final CallLogRepository callLogRepository;

    public CommunicationQueryService(CallLogRepository callLogRepository) {
        this.callLogRepository = callLogRepository;
    }

    public WristbandCapabilityProfile getDeviceCapabilities(FragileCitizenId fragileCitizenId) {
        String uuidText = fragileCitizenId.value().toString().toLowerCase();
        String citizenPhone = "+51 987 654 321";
        String caregiverPhone = "+51 912 345 678";

        // Permite probar determinísticamente los 3 escenarios según el sufijo o valor del UUID
        char lastChar = uuidText.charAt(uuidText.length() - 1);
        if (lastChar == '2' || uuidText.startsWith("22222222")) {
            return WristbandCapabilityProfile.guardianBandLite(citizenPhone, caregiverPhone);
        }
        if (lastChar == '3' || uuidText.startsWith("33333333")) {
            return WristbandCapabilityProfile.guardianCore(citizenPhone, caregiverPhone);
        }
        return WristbandCapabilityProfile.guardianPro(citizenPhone, caregiverPhone);
    }

    public List<CallLogEntry> getCallLogs(FragileCitizenId fragileCitizenId) {
        return callLogRepository.findByFragileCitizenIdOrderByOccurredAtDesc(fragileCitizenId);
    }
}
