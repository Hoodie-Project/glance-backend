package com.hoodiev.glance.common;

import com.hoodiev.glance.comment.Comment;
import com.hoodiev.glance.comment.CommentLike;
import com.hoodiev.glance.comment.CommentLikeRepository;
import com.hoodiev.glance.comment.CommentRepository;
import com.hoodiev.glance.region.GeocodingService;
import com.hoodiev.glance.region.LocationInfo;
import com.hoodiev.glance.region.Region;
import com.hoodiev.glance.region.RegionRepository;
import com.hoodiev.glance.thread.AnimalLook;
import com.hoodiev.glance.thread.Gender;
import com.hoodiev.glance.thread.Thread;
import com.hoodiev.glance.thread.ThreadLike;
import com.hoodiev.glance.thread.ThreadLikeRepository;
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
    private final ThreadLikeRepository threadLikeRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final RegionRepository regionRepository;
    private final GeocodingService geocodingService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (threadRepository.count() > 0) return;

        String pw = passwordEncoder.encode("1234");

        Thread t1 = threadRepository.save(Thread.builder()
                .nickname("산책러버")
                .title("홍대 걷다가 진짜 예쁜 분 봤어요")
                .content("오후 3시쯤 홍대 걷고있었는데 흰색 린넨 셔츠에 청바지 입으신 분 지나가셨어요. 키 크고 얼굴도 너무 예쁘셨는데 혹시 아시는 분?")
                .latitude(37.5563).longitude(126.9236)
                .region(resolveRegion(37.5563, 126.9236))
                .gender(Gender.FEMALE)
                .tags(List.of("홍대", "린넨셔츠", "오후산책"))
                .animalLooks(Set.of(AnimalLook.FOX, AnimalLook.CAT))
                .vibeStyles(Set.of(VibeStyle.COLD_BEAUTY))
                .password(pw).build());

        Thread t2 = threadRepository.save(Thread.builder()
                .nickname("카페탐방러")
                .title("성수 카페에서 노트북 하시던 분")
                .content("성수 어느 카페 2층 창가 자리에서 노트북 하고 계셨는데 안경 쓰고 검정 후디 입고 계셨어요. 엄청 집중해서 일하시는 모습이 멋있었습니다")
                .latitude(37.5448).longitude(127.0557)
                .region(resolveRegion(37.5448, 127.0557))
                .gender(Gender.MALE)
                .tags(List.of("성수", "카페", "노트북"))
                .animalLooks(Set.of(AnimalLook.DOG))
                .vibeStyles(Set.of(VibeStyle.WARM_HANDSOME))
                .password(pw).build());

        Thread t3 = threadRepository.save(Thread.builder()
                .nickname("지하철킬러")
                .title("2호선에서 책 읽던 분 너무 설레요")
                .content("강남역에서 탔는데 홍대입구까지 계속 소설책 읽으시더라고요. 베이지색 코트에 짧은 단발. 내리실 때 살짝 웃으셨는데 심장이...")
                .latitude(37.4979).longitude(127.0276)
                .region(resolveRegion(37.4979, 127.0276))
                .gender(Gender.FEMALE)
                .tags(List.of("2호선", "강남", "독서"))
                .animalLooks(Set.of(AnimalLook.RABBIT, AnimalLook.DEER))
                .vibeStyles(Set.of(VibeStyle.HARMLESS, VibeStyle.FRESH))
                .password(pw).build());

        Thread t4 = threadRepository.save(Thread.builder()
                .nickname("뚝섬러")
                .title("뚝섬 한강공원 러닝하시던 분")
                .content("저녁 7시쯤 뚝섬 한강에서 파란 러닝복 입고 뛰시던 분 계셨어요. 키 엄청 크시고 달리는 폼도 너무 멋있었는데 매일 오시나요?")
                .latitude(37.5311).longitude(127.0674)
                .region(resolveRegion(37.5311, 127.0674))
                .gender(Gender.MALE)
                .tags(List.of("한강", "뚝섬", "러닝"))
                .animalLooks(Set.of(AnimalLook.WOLF))
                .vibeStyles(Set.of(VibeStyle.COLD_HANDSOME, VibeStyle.CLASSIC_HANDSOME))
                .password(pw).build());

        Thread t5 = threadRepository.save(Thread.builder()
                .nickname("망원시장단골")
                .title("망원시장 떡볶이집 앞에서 봤어요")
                .content("주말 낮에 망원시장 떡볶이 먹으면서 친구랑 웃으시던 분. 빨간 니트에 흰 운동화. 웃는 모습이 너무 귀여우셨어요 ㅠㅠ")
                .latitude(37.5551).longitude(126.9097)
                .region(resolveRegion(37.5551, 126.9097))
                .gender(Gender.FEMALE)
                .tags(List.of("망원", "망원시장", "주말"))
                .animalLooks(Set.of(AnimalLook.HAMSTER, AnimalLook.RABBIT))
                .vibeStyles(Set.of(VibeStyle.WARM_BEAUTY, VibeStyle.FRESH))
                .password(pw).build());

        // 게시글 좋아요
        addThreadLike(t1, "1.1.1.1"); addThreadLike(t1, "1.1.1.2"); addThreadLike(t1, "1.1.1.3");
        addThreadLike(t1, "1.1.1.4"); addThreadLike(t1, "1.1.1.5");
        addThreadLike(t2, "1.1.2.1"); addThreadLike(t2, "1.1.2.2"); addThreadLike(t2, "1.1.2.3");
        addThreadLike(t3, "1.1.3.1"); addThreadLike(t3, "1.1.3.2"); addThreadLike(t3, "1.1.3.3");
        addThreadLike(t3, "1.1.3.4"); addThreadLike(t3, "1.1.3.5"); addThreadLike(t3, "1.1.3.6");
        addThreadLike(t3, "1.1.3.7");
        addThreadLike(t4, "1.1.4.1"); addThreadLike(t4, "1.1.4.2");
        addThreadLike(t5, "1.1.5.1"); addThreadLike(t5, "1.1.5.2"); addThreadLike(t5, "1.1.5.3");
        addThreadLike(t5, "1.1.5.4");

        // 댓글
        Comment c1 = addComment(t1, "궁금한사람", "혹시 머리 길이가 어느 정도였나요? 제 친구일 수도 있어서요 ㅋㅋ", pw);
        Comment c2 = addComment(t1, "나도목격", "저도 그 시간대에 홍대 있었는데 혹시 웨이브 머리셨나요?", pw);
        Comment c3 = addComment(t1, "응원러", "용기내서 말 걸어보세요!! 응원합니다", pw);
        Comment c4 = addComment(t2, "성수고정", "성수에 그런 분 많아요 ㅎㅎ 어느 카페였는지 힌트 주실 수 있나요?", pw);
        Comment c5 = addComment(t2, "개발자아닐까", "노트북에 뭔가 붙어있었나요? 스티커라든지", pw);
        Comment c6 = addComment(t3, "2호선러버", "2호선은 진짜 설레는 일이 많이 일어나는 것 같아요", pw);
        Comment c7 = addComment(t3, "책추천러", "어떤 책 읽고 계셨는지 혹시 표지 보셨나요?", pw);
        Comment c8 = addComment(t4, "뚝섬러너", "뚝섬 저도 자주 뛰어요! 오늘도 거기 있었는데", pw);
        Comment c9 = addComment(t5, "망원토박이", "망원시장 그 떡볶이집 맛있죠 ㅋㅋ 저도 자주 가는데", pw);
        Comment c10 = addComment(t5, "빨간니트탐정", "빨간 니트에 흰 운동화면 저 아는 사람 같기도..? 이름이 어떻게 되시나요", pw);

        // 댓글 좋아요
        addCommentLike(c1, "2.1.1.1"); addCommentLike(c1, "2.1.1.2"); addCommentLike(c1, "2.1.1.3");
        addCommentLike(c3, "2.1.3.1"); addCommentLike(c3, "2.1.3.2");
        addCommentLike(c4, "2.2.4.1");
        addCommentLike(c6, "2.3.6.1"); addCommentLike(c6, "2.3.6.2"); addCommentLike(c6, "2.3.6.3");
        addCommentLike(c7, "2.3.7.1"); addCommentLike(c7, "2.3.7.2");
        addCommentLike(c10, "2.5.10.1"); addCommentLike(c10, "2.5.10.2");
    }

    private Comment addComment(Thread thread, String nickname, String content, String pw) {
        Comment comment = commentRepository.save(Comment.builder()
                .thread(thread).nickname(nickname).content(content).password(pw).build());
        threadRepository.incrementCommentCount(thread.getId());
        return comment;
    }

    private void addThreadLike(Thread thread, String ip) {
        threadLikeRepository.save(ThreadLike.builder().threadId(thread.getId()).ipAddress(ip).build());
        threadRepository.incrementLikeCount(thread.getId());
    }

    private void addCommentLike(Comment comment, String ip) {
        commentLikeRepository.save(CommentLike.builder().commentId(comment.getId()).ipAddress(ip).build());
        commentRepository.incrementLikeCount(comment.getId());
    }

    private Region resolveRegion(double lat, double lng) {
        LocationInfo info = geocodingService.reverseGeocode(lat, lng);
        if (info == null) return null;
        return regionRepository.findByLegalCode(info.legalCode())
                .orElseGet(() -> regionRepository.save(Region.builder()
                        .legalCode(info.legalCode())
                        .sido(info.sido())
                        .sigungu(info.sigungu())
                        .dong(info.dong())
                        .build()));
    }
}
