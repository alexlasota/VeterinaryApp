package pl.gr.veterinaryapp.model.dto;

import lombok.Data;
import pl.gr.veterinaryapp.common.VisitStatus;

@Data
public class VisitEditDto {

    private Long id;
    private String description;
    private VisitStatus visitStatus;
}
