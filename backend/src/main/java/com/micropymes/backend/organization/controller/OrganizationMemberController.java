package com.micropymes.backend.organization.controller;

import com.micropymes.backend.auth.security.AuthenticatedUser;
import com.micropymes.backend.organization.dto.AddMemberRequest;
import com.micropymes.backend.organization.dto.MemberResponse;
import com.micropymes.backend.organization.dto.UpdateMemberRoleRequest;
import com.micropymes.backend.organization.service.OrganizationMemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}/members"
)
@Tag(name = "Organization Members", description = "Organization member management")
public class OrganizationMemberController {

    private final OrganizationMemberService memberService;

    public OrganizationMemberController(
            OrganizationMemberService memberService
    ) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberResponse> findAll(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return memberService.findAll(
                organizationId,
                principal.userId()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse addMember(
            @PathVariable UUID organizationId,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return memberService.addMember(
                organizationId,
                principal.userId(),
                request
        );
    }

    @PatchMapping("/{memberId}")
    public MemberResponse changeRole(
            @PathVariable UUID organizationId,
            @PathVariable UUID memberId,
            @Valid
            @RequestBody UpdateMemberRoleRequest request,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return memberService.changeRole(
                organizationId,
                memberId,
                principal.userId(),
                request
        );
    }

    @PostMapping("/{memberId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(
            @PathVariable UUID organizationId,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        memberService.deactivate(
                organizationId,
                memberId,
                principal.userId()
        );
    }
}