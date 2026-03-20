package ma.fellahia.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /**
     * Arabic/Darija legal assistant system prompt.
     * The model used is configured in application.yml under spring.ai.ollama.chat.model
     * Recommended: "aya" (Cohere Aya — strong Arabic/Darija support)
     * Alternatives: "aya-expanse", "llama3.2", "mistral"
     */
    private static final String SYSTEM_PROMPT = """
            أنت مساعد قانوني متخصص في القانون الفلاحي المغربي اسمك FellahIA.
            مهمتك مساعدة الفلاحين في فهم حقوقهم القانونية المتعلقة بـ:
            - الأراضي الفلاحية وعقود البيع والكراء
            - حقوق المياه والري
            - النزاعات العقارية الفلاحية
            - التعاونيات الفلاحية والقوانين المنظمة لها
            - الدعم الحكومي ومساطر الاستفادة منه

            قواعد مهمة:
            - أجب بالدارجة المغربية أو العربية الفصحى حسب لغة السؤال
            - كن واضحاً ومبسطاً، تجنب المصطلحات القانونية المعقدة
            - وجه المستخدم لاستشارة محامي متخصص للقضايا المعقدة
            - لا تعطي استشارات قانونية قاطعة تتعلق بقضايا بعينها
            """;

    @Bean
    public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }


}
