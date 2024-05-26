package com.bartslota.availability.infrastructure.rest

import com.bartslota.availability.IntegrationSpec
import com.bartslota.availability.application.AssetAvailabilityStoreSupport
import com.bartslota.availability.domain.AssetAvailability
import com.bartslota.availability.domain.AssetAvailabilityRepository
import com.bartslota.availability.domain.OwnerId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.ResultActions

import static com.bartslota.availability.domain.AssetIdFixture.someAssetIdValue
import static com.bartslota.availability.domain.DurationFixture.someValidDuration
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AssetAvailabilityControllerIT extends IntegrationSpec implements AssetAvailabilityStoreSupport {

    @Autowired
    private AssetAvailabilityRepository assetAvailabilityRepository

    def "should accept the registration of new asset"() {
        given:
            String assetId = someAssetIdValue()

        when:
            ResultActions result = mockMvc.perform(
                    post("/api/assets/commands")
                            .with(httpBasic("ADMIN_123", "admin_pass_123"))
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {
                                    "type" : "REGISTER",
                                    "assetId" : "${assetId}"
                                }
                            """))

        then:
            result.andExpect(status().isAccepted())
    }

    def "should reject the registration of already existing asset"() {
        given:
            AssetAvailability asset = existingAsset()

        when:
            ResultActions result = mockMvc.perform(
                    post("/api/assets/commands")
                            .with(httpBasic("ADMIN_123", "admin_pass_123"))
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {
                                    "type" : "REGISTER",
                                    "assetId" : "${asset.id().asString()}"
                                }
                            """))

        then:
            result.andExpect(status().isUnprocessableEntity())
    }

    def "should accept the withdrawal of existing asset"() {
        given:
            AssetAvailability asset = existingAsset()

        when:
            ResultActions result = mockMvc.perform(
                    post("/api/assets/commands")
                            .with(httpBasic("ADMIN_123", "admin_pass_123"))
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {
                                    "type" : "WITHDRAW",
                                    "assetId" : "${asset.id().asString()}"
                                }
                            """))

        then:
            result.andExpect(status().isAccepted())
    }

    def "should reject the withdrawal of locked asset"() {
        given:
            AssetAvailability asset = lockedAsset()

        when:
            ResultActions result = mockMvc.perform(
                    post("/api/assets/commands")
                            .with(httpBasic("ADMIN_123", "admin_pass_123"))
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {
                                    "type" : "WITHDRAW",
                                    "assetId" : "${asset.id().asString()}"
                                }
                            """))

        then:
            result.andExpect(status().isUnprocessableEntity())
    }

    def "should accept the activation of registered asset"() {
        given:
            AssetAvailability asset = existingAsset()

        when:
            ResultActions result = mockMvc.perform(
                    post("/api/assets/commands")
                            .with(httpBasic("ADMIN_123", "admin_pass_123"))
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {
                                    "type" : "ACTIVATE",
                                    "assetId" : "${asset.id().asString()}"
                                }
                            """))

        then:
            result.andExpect(status().isAccepted())
    }

    def "should reject the activation of not existing asset"() {
        given:
            String idOfNotExistingAsset = someAssetIdValue()

        when:
            ResultActions result = mockMvc.perform(
                    post("/api/assets/commands")
                            .with(httpBasic("ADMIN_123", "admin_pass_123"))
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {
                                    "type" : "ACTIVATE",
                                    "assetId" : "${idOfNotExistingAsset}"
                                }
                            """))

        then:
            result.andExpect(status().isUnprocessableEntity())
    }

    def "should accept the locking of an activated asset"() {
        given:
            AssetAvailability asset = activatedAsset()

        when:
            ResultActions result = mockMvc.perform(
                    post("/api/assets/commands")
                            .with(httpBasic("ADAM_123", "pass_123"))
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {
                                    "type" : "LOCK",
                                    "assetId" : "${asset.id().asString()}",
                                    "durationInMinutes" : "${someValidDuration().toMinutes()}"
                                }
                            """))

        then:
            result.andExpect(status().isAccepted())
    }

    def "should reject the locking of locked asset"() {
        given:
            AssetAvailability asset = lockedAsset()

        when:
            ResultActions result = mockMvc.perform(
                    post("/api/assets/commands")
                            .with(httpBasic("ADAM_123", "pass_123"))
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {
                                    "type" : "LOCK",
                                    "assetId" : "${asset.id().asString()}",
                                    "durationInMinutes" : "${someValidDuration().toMinutes()}"
                                }
                            """))

        then:
            result.andExpect(status().isUnprocessableEntity())
    }

    def "should accept the indefinite locking of already locked asset"() {
        given:
            AssetAvailability asset = assetLockedBy(OwnerId.of("ADAM_123"))

        when:
            ResultActions result = mockMvc.perform(
                    post("/api/assets/commands")
                            .with(httpBasic("ADAM_123", "pass_123"))
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {
                                    "type" : "LOCK_INDEFINITELY",
                                    "assetId" : "${asset.id().asString()}"
                                }
                            """))

        then:
            result.andExpect(status().isAccepted())
    }

    def "should reject the indefinite locking of asset locked by someone else"() {
        given:
            AssetAvailability asset = lockedAsset()

        when:
            ResultActions result = mockMvc.perform(
                    post("/api/assets/commands")
                            .with(httpBasic("ADAM_123", "pass_123"))
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {
                                    "type" : "LOCK_INDEFINITELY",
                                    "assetId" : "${asset.id().asString()}"
                                }
                            """))

        then:
            result.andExpect(status().isUnprocessableEntity())
    }

    def "should accept the unlocking of already locked asset"() {
        given:
            AssetAvailability asset = assetLockedBy(OwnerId.of("ADAM_123"))

        when:
            ResultActions result = mockMvc.perform(
                    post("/api/assets/commands")
                            .with(httpBasic("ADAM_123", "pass_123"))
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {
                                    "type" : "UNLOCK",
                                    "assetId" : "${asset.id().asString()}"
                                }
                            """))

        then:
            result.andExpect(status().isAccepted())
    }

    def "should reject the unlocking of asset locked by someone else"() {
        given:
            AssetAvailability asset = lockedAsset()

        when:
            ResultActions result = mockMvc.perform(
                    post("/api/assets/commands")
                            .with(httpBasic("ADAM_123", "pass_123"))
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {
                                    "type" : "UNLOCK",
                                    "assetId" : "${asset.id().asString()}"
                                }
                            """))

        then:
            result.andExpect(status().isUnprocessableEntity())
    }

    @Override
    AssetAvailabilityRepository repository() {
        return assetAvailabilityRepository
    }
}
