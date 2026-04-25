package com.hoodiev.glance.common;

import com.hoodiev.glance.comment.Comment;
import com.hoodiev.glance.comment.CommentRepository;
import com.hoodiev.glance.thread.AnimalLook;
import com.hoodiev.glance.thread.Gender;
import com.hoodiev.glance.thread.Thread;
import com.hoodiev.glance.thread.ThreadRepository;
import com.hoodiev.glance.thread.VibeStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Profile("!prod")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (threadRepository.count() > 0) return;

        String pw = passwordEncoder.encode("1234");

        Thread t1 = threadRepository.save(Thread.builder()
                .nickname("산책러버")
                .title("홍대 걷다가 진짜 예쁜 분 봤어요")
                .content("오후 3시쯤 홍대 걷고있었는데 흰색 린넨 셔츠에 청바지 입으신 분 지나가셨어요. 키 크고 얼굴도 너무 예쁘셨는데 혹시 아시는 분?")
                .latitude(37.5563)
                .longitude(126.9236)
                .gender(Gender.FEMALE)
                .tags(List.of("홍대", "린넨셔츠", "오후산책"))
                .animalLooks(Set.of(AnimalLook.FOX, AnimalLook.CAT))
                .vibeStyles(Set.of(VibeStyle.COLD_BEAUTY))
                .password(pw)
                .build());

        Thread t2 = threadRepository.save(Thread.builder()
                .nickname("카페탐방러")
                .title("성수 카페에서 노트북 하시던 분")
                .content("성수 어느 카페 2층 창가 자리에서 노트북 하고 계셨는데 안경 쓰고 검정 후디 입고 계셨어요. 엄청 집중해서 일하시는 모습이 멋있었습니다")
                .latitude(37.5448)
                .longitude(127.0557)
                .gender(Gender.MALE)
                .tags(List.of("성수", "카페", "노트북"))
                .animalLooks(Set.of(AnimalLook.DOG))
                .vibeStyles(Set.of(VibeStyle.WARM_HANDSOME))
                .password(pw)
                .build());

        Thread t3 = threadRepository.save(Thread.builder()
                .nickname("지하철킬러")
                .title("2호선에서 책 읽던 분 너무 설레요")
                .content("강남역에서 탔는데 홍대입구까지 계속 소설책 읽으시더라고요. 베이지색 코트에 짧은 단발. 내리실 때 살짝 웃으셨는데 심장이...")
                .latitude(37.4979)
                .longitude(127.0276)
                .gender(Gender.FEMALE)
                .tags(List.of("2호선", "강남", "독서"))
                .animalLooks(Set.of(AnimalLook.RABBIT, AnimalLook.DEER))
                .vibeStyles(Set.of(VibeStyle.HARMLESS, VibeStyle.FRESH))
                .password(pw)
                .build());

        Thread t4 = threadRepository.save(Thread.builder()
                .nickname("뚝섬러")
                .title("뚝섬 한강공원 러닝하시던 분")
                .content("저녁 7시쯤 뚝섬 한강에서 파란 러닝복 입고 뛰시던 분 계셨어요. 키 엄청 크시고 달리는 폼도 너무 멋있었는데 매일 오시나요?")
                .latitude(37.5311)
                .longitude(127.0674)
                .gender(Gender.MALE)
                .tags(List.of("한강", "뚝섬", "러닝"))
                .animalLooks(Set.of(AnimalLook.WOLF))
                .vibeStyles(Set.of(VibeStyle.COLD_HANDSOME, VibeStyle.CLASSIC_HANDSOME))
                .password(pw)
                .build());

        Thread t5 = threadRepository.save(Thread.builder()
                .nickname("망원시장단골")
                .title("망원시장 떡볶이집 앞에서 봤어요")
                .content("주말 낮에 망원시장 떡볶이 먹으면서 친구랑 웃으시던 분. 빨간 니트에 흰 운동화. 웃는 모습이 너무 귀여우셨어요 ㅠㅠ")
                .latitude(37.5551)
                .longitude(126.9097)
                .gender(Gender.FEMALE)
                .tags(List.of("망원", "망원시장", "주말"))
                .animalLooks(Set.of(AnimalLook.HAMSTER, AnimalLook.RABBIT))
                .vibeStyles(Set.of(VibeStyle.WARM_BEAUTY, VibeStyle.FRESH))
                .password(pw)
                .build());

        commentRepository.save(Comment.builder()
                .thread(t1).nickname("궁금한사람")
                .content("혹시 머리 길이가 어느 정도였나요? 제 친구일 수도 있어서요 ㅋㅋ")
                .password(pw).build());

        commentRepository.save(Comment.builder()
                .thread(t1).nickname("나도목격")
                .content("저도 그 시간대에 홍대 있었는데 혹시 웨이브 머리셨나요?")
                .password(pw).build());

        commentRepository.save(Comment.builder()
                .thread(t1).nickname("응원러")
                .content("용기내서 말 걸어보세요!! 응원합니다")
                .password(pw).build());

        commentRepository.save(Comment.builder()
                .thread(t2).nickname("성수고정")
                .content("성수에 그런 분 많아요 ㅎㅎ 어느 카페였는지 힌트 주실 수 있나요?")
                .password(pw).build());

        commentRepository.save(Comment.builder()
                .thread(t2).nickname("개발자아닐까")
                .content("노트북에 뭔가 붙어있었나요? 스티커라든지")
                .password(pw).build());

        commentRepository.save(Comment.builder()
                .thread(t3).nickname("2호선러버")
                .content("2호선은 진짜 설레는 일이 많이 일어나는 것 같아요")
                .password(pw).build());

        commentRepository.save(Comment.builder()
                .thread(t3).nickname("책추천러")
                .content("어떤 책 읽고 계셨는지 혹시 표지 보셨나요?")
                .password(pw).build());

        commentRepository.save(Comment.builder()
                .thread(t4).nickname("뚝섬러너")
                .content("뚝섬 저도 자주 뛰어요! 오늘도 거기 있었는데")
                .password(pw).build());

        commentRepository.save(Comment.builder()
                .thread(t5).nickname("망원토박이")
                .content("망원시장 그 떡볶이집 맛있죠 ㅋㅋ 저도 자주 가는데")
                .password(pw).build());

        commentRepository.save(Comment.builder()
                .thread(t5).nickname("빨간니트탐정")
                .content("빨간 니트에 흰 운동화면 저 아는 사람 같기도..? 이름이 어떻게 되시나요")
                .password(pw).build());
    }
}
