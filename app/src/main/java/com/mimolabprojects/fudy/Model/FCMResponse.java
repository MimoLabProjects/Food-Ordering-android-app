package com.mimolabprojects.fudy.Model;

import java.util.List;

public class FCMResponse {
    private long multicast_id;
    private int success, failure, canonical_ids;
    private List <FCMResult> results;
    private long message_id;

}
