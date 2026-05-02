package org.adch.multimodalparliamentexplorer.member;

import lombok.AllArgsConstructor;
import org.adch.multimodalparliamentexplorer.member.dto.MemberListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
@AllArgsConstructor
public class ParliamentMemberController {

    private ParliamentMemberService memberService;

    @GetMapping
    public ResponseEntity<Page<MemberListDto>> getAllMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size,  Sort.by("lastName").ascending());

        return ResponseEntity.ok(memberService.getAllMembers(pageable));
    }

    @GetMapping("/parties")
    public ResponseEntity<?> getAllParties() {
        return ResponseEntity.ok(memberService.getDistinctParties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParliamentMember> getMember(@PathVariable String id){

        var member = memberService.getMember(id);

        return member.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

    }

}
