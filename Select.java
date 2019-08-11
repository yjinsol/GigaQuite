package com.example.kd.quiet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class Select extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select);

        getWindow().getDecorView().setSystemUiVisibility(

                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |

                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |

                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |

                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |

                        View.SYSTEM_UI_FLAG_FULLSCREEN |

                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        );
    }

    public void bbc(View view){
        Intent intent = new Intent(getApplicationContext(),Bbc.class);
        startActivity(intent);
    }

    public void quiet(View view){
        Intent intent = new Intent(getApplicationContext(),Quiet.class);
        startActivity(intent);
    }

    public void setting(View view){

        Intent intent = new Intent(getApplicationContext(),Setting.class);
        startActivity(intent);
    }
}