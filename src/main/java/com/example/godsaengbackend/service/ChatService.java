package com.example.godsaengbackend.service;

import com.example.godsaengbackend.dto.ChatDto;
import com.example.godsaengbackend.entity.ChatMessage;
import com.example.godsaengbackend.entity.ChatSession;
import com.example.godsaengbackend.entity.Lecture;
import com.example.godsaengbackend.entity.User;
import com.example.godsaengbackend.repository.ChatMessageRepository;
import com.example.godsaengbackend.repository.ChatSessionRepository;
import com.example.godsaengbackend.repository.LectureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final LectureRepository lectureRepository;
    private final UserService userService;
    private final AIService aiService;

    public ChatService(ChatSessionRepository chatSessionRepository,
                       ChatMessageRepository chatMessageRepository,
                       LectureRepository lectureRepository,
                       UserService userService,
                       AIService aiService) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.lectureRepository = lectureRepository;
        this.userService = userService;
        this.aiService = aiService;
    }

    @Transactional
    public ChatDto.SessionResponse createChatSession(String email, ChatDto.CreateSessionRequest request) {
        User user = userService.findByEmail(email);
        
        // 갓생 모드가 아닌데 NORMAL 이외의 말투를 선택한 경우 체크
        if (!user.getIsGodMode() && request.getTone() != ChatSession.ToneType.NORMAL) {
            logger.warn("일반 사용자({})가 특별 말투를 선택했습니다. 기본 말투로 변경합니다.", email);
            request.setTone(ChatSession.ToneType.NORMAL);
        }
        
        Lecture lecture = lectureRepository.findById(request.getLectureId())
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));
        
        // 사용자가 해당 강의의 소유자인지 확인
        if (!lecture.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("해당 강의에 대한 권한이 없습니다.");
        }
        
        // 강의 처리가 완료되었는지 확인
        if (lecture.getStatus() != Lecture.LectureStatus.COMPLETED) {
            throw new RuntimeException("아직 처리가 완료되지 않은 강의입니다.");
        }
        
        // 세션 제목이 없으면 강의 제목으로 설정
        String title = request.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = lecture.getTitle() + " 채팅";
        }
        
        ChatSession chatSession = ChatSession.builder()
                .user(user)
                .lecture(lecture)
                .title(title)
                .tone(request.getTone())
                .build();
        
        ChatSession savedSession = chatSessionRepository.save(chatSession);
        
        // 시스템 메시지 추가 (AI에게 강의 내용 컨텍스트 제공)
        String welcomeMessage = "안녕하세요! " + lecture.getTitle() + "에 대해 질문해 주세요. 강의 내용을 바탕으로 답변해 드리겠습니다.";
        
        ChatMessage systemMessage = ChatMessage.builder()
                .chatSession(savedSession)
                .role(ChatMessage.MessageRole.ASSISTANT)
                .content(welcomeMessage)
                .build();
        
        chatMessageRepository.save(systemMessage);
        
        return ChatDto.SessionResponse.fromEntity(savedSession);
    }

    // 말투 옵션 목록 조회
    @Transactional(readOnly = true)
    public List<ChatDto.ToneOption> getToneOptions() {
        return ChatDto.ToneOption.getAllTones();
    }

    // 사용 가능한 말투 옵션 목록 조회 (구독 상태에 따라 다름)
    @Transactional(readOnly = true)
    public List<ChatDto.ToneOption> getAvailableToneOptions(String email) {
        User user = userService.findByEmail(email);
        
        if (user.getIsGodMode()) {
            // 갓생 모드는 모든 말투 사용 가능
            return ChatDto.ToneOption.getAllTones();
        } else {
            // 일반 모드는 기본 말투만 사용 가능
            return Arrays.stream(new ChatSession.ToneType[]{ChatSession.ToneType.NORMAL})
                    .map(tone -> new ChatDto.ToneOption(tone.name(), tone.getDescription()))
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    public ChatDto.MessageResponse sendMessage(String email, Long sessionId, ChatDto.MessageRequest request) {
        User user = userService.findByEmail(email);
        ChatSession session = chatSessionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow(() -> new RuntimeException("채팅 세션을 찾을 수 없습니다."));
        
        // 사용자 메시지 저장
        ChatMessage userMessage = ChatMessage.builder()
                .chatSession(session)
                .role(ChatMessage.MessageRole.USER)
                .content(request.getContent())
                .build();
        
        ChatMessage savedUserMessage = chatMessageRepository.save(userMessage);
        
        // AI 응답 요청 (말투 정보 포함)
        String aiResponse = aiService.requestChatResponse(
                session.getLecture().getId(),
                session.getId(),
                request.getContent(),
                session.getMessages(),
                session.getTone()  // 말투 정보 추가
        );
        
        // AI 응답 저장
        ChatMessage assistantMessage = ChatMessage.builder()
                .chatSession(session)
                .role(ChatMessage.MessageRole.ASSISTANT)
                .content(aiResponse)
                .build();
        
        ChatMessage savedAssistantMessage = chatMessageRepository.save(assistantMessage);
        
        return ChatDto.MessageResponse.fromEntity(savedAssistantMessage);
    }

    @Transactional(readOnly = true)
    public Page<ChatDto.SessionResponse> getChatSessions(String email, Pageable pageable) {
        User user = userService.findByEmail(email);
        Page<ChatSession> sessions = chatSessionRepository.findByUser(user, pageable);
        return sessions.map(ChatDto.SessionResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ChatDto.SessionResponse> getChatSessionsByLecture(String email, Long lectureId, Pageable pageable) {
        User user = userService.findByEmail(email);
        Page<ChatSession> sessions = chatSessionRepository.findByUserAndLecture_Id(user, lectureId, pageable);
        return sessions.map(ChatDto.SessionResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ChatDto.SessionDetailResponse getChatSession(String email, Long sessionId) {
        User user = userService.findByEmail(email);
        ChatSession session = chatSessionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow(() -> new RuntimeException("채팅 세션을 찾을 수 없습니다."));
        
        List<ChatMessage> messages = session.getMessages();
        
        return ChatDto.SessionDetailResponse.fromEntity(session, messages);
    }

    @Transactional
    public void deleteChatSession(String email, Long sessionId) {
        User user = userService.findByEmail(email);
        ChatSession session = chatSessionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow(() -> new RuntimeException("채팅 세션을 찾을 수 없습니다."));
        
        chatSessionRepository.delete(session);
    }
} 