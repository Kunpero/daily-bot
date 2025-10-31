package rs.kunperooo.dailybot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rs.kunperooo.dailybot.controller.dto.CheckInFormData;
import rs.kunperooo.dailybot.controller.dto.CheckInRestData;
import rs.kunperooo.dailybot.service.CheckInService;
import rs.kunperooo.dailybot.service.SlackApiService;
import rs.kunperooo.dailybot.service.dto.SlackUserDto;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/checkin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class CheckInController {

    private final CheckInService checkInService;
    private final SlackApiService slackApiService;

    @GetMapping
    public String listCheckIns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String search,
            Model model) {

        log.debug("Listing check-ins - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CheckInRestData> checkInPage;

        if (owner != null && !owner.trim().isEmpty()) {
            checkInPage = checkInService.findByOwner(owner, pageable);
        } else {
            // For admin, we need to get all check-ins - this would need a different service method
            // For now, we'll use a workaround by getting all check-ins and paginating manually
            List<CheckInRestData> allCheckIns = checkInService.findAll();
            checkInPage = createPageFromList(allCheckIns, pageable);
        }

        model.addAttribute("checkInPage", checkInPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", checkInPage.getTotalPages());
        model.addAttribute("totalElements", checkInPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("owner", owner);
        model.addAttribute("search", search);

        return "checkin";
    }

    @GetMapping("/{uuid}")
    public String viewCheckIn(@PathVariable UUID uuid, Model model) {
        log.debug("Viewing check-in with ID: {}", uuid);

        CheckInRestData checkInForm = checkInService.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Check-in not found with ID: " + uuid));

        model.addAttribute("checkInForm", checkInForm);
        return "checkin-detail";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        log.debug("Showing create check-in form");

        CheckInFormData checkInForm = CheckInFormData.builder()
                .build();
        List<SlackUserDto> activeUsers = slackApiService.getActiveUsers();

        model.addAttribute("checkInForm", checkInForm);
        model.addAttribute("isEdit", false);
        model.addAttribute("activeUsers", activeUsers);
        return "checkin-form";
    }

    @GetMapping("/{uuid}/edit")
    public String showEditForm(@PathVariable UUID uuid, Model model) {
        log.debug("Showing edit form for check-in ID: {}", uuid);

        CheckInRestData checkInForm = checkInService.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Check-in not found with ID: " + uuid));
        List<SlackUserDto> activeUsers = slackApiService.getActiveUsers();

        model.addAttribute("checkInForm", checkInForm);
        model.addAttribute("isEdit", true);
        model.addAttribute("activeUsers", activeUsers);

        return "checkin-form";
    }

    @PostMapping("/create")
    public String createCheckIn(
            @ModelAttribute CheckInFormData checkInForm,
            @AuthenticationPrincipal OidcUser principal,
            RedirectAttributes redirectAttributes) {

        log.info("Creating new check-in for owner: {} with {} questions",
                principal.getSubject(),
                checkInForm.getQuestions() != null ? checkInForm.getQuestions().size() : 0);

        try {
            checkInService.createCheckIn(
                    principal.getSubject(),
                    checkInForm.getName(),
                    checkInForm.getIntroMessage(),
                    checkInForm.getOutroMessage(),
                    checkInForm.getQuestions(),
                    checkInForm.getMembers()
            );

            redirectAttributes.addFlashAttribute("successMessage", "Check-in created successfully!");
            return "redirect:/checkin";
        } catch (Exception e) {
            log.error("Error creating check-in", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating check-in: " + e.getMessage());
            return "redirect:/checkin/create";
        }
    }

    @PostMapping("/{uuid}/edit")
    public String updateCheckIn(
            @PathVariable UUID uuid,
            @ModelAttribute CheckInFormData checkInForm,
            @AuthenticationPrincipal OidcUser principal,
            RedirectAttributes redirectAttributes) {

        log.info("Updating check-in ID: {} for owner: {} with {} questions",
                uuid,
                principal.getSubject(),
                checkInForm.getQuestions() != null ? checkInForm.getQuestions().size() : 0);

        try {
            checkInService.updateCheckIn(
                    uuid,
                    principal.getSubject(),
                    checkInForm.getName(),
                    checkInForm.getIntroMessage(),
                    checkInForm.getOutroMessage(),
                    checkInForm.getQuestions(),
                    checkInForm.getMembers()
            );

            redirectAttributes.addFlashAttribute("successMessage", "Check-in updated successfully!");
            return "redirect:/checkin";
        } catch (Exception e) {
            log.error("Error updating check-in", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating check-in: " + e.getMessage());
            return "redirect:/checkin/" + uuid + "/edit";
        }
    }

    @PostMapping("/{uuid}/delete")
    public String deleteCheckIn(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal OidcUser principal,
            RedirectAttributes redirectAttributes) {

        log.info("Deleting check-in ID: {} for owner: {}", uuid, principal.getSubject());

        try {
            checkInService.deleteCheckIn(uuid, principal.getSubject());
            redirectAttributes.addFlashAttribute("successMessage", "Check-in deleted successfully!");
        } catch (Exception e) {
            log.error("Error deleting check-in", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting check-in: " + e.getMessage());
        }

        return "redirect:/checkin";
    }

    /**
     * Helper method to create a Page from a List (for admin view)
     */
    private Page<CheckInRestData> createPageFromList(List<CheckInRestData> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        if (start > list.size()) {
            return Page.empty(pageable);
        }

        List<CheckInRestData> pageContent = list.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, list.size());
    }
}